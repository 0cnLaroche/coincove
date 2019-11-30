package webserver.service;

import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import webserver.model.Payment;
import webserver.repository.TransactionRepository;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class BitcoinService {

    private WalletAppKit kit;
    private NetworkParameters params;
    private String forwardingAddressHash;
    private String networkName;
    private String filePrefix;
    private Map<String, Transaction> pendingTransactionMap;
    private TransactionRepository transactionRepository;

    private static final double EQUIVALENCY_MARGIN = 0.01;

    private static final Logger LOG = LoggerFactory.getLogger(BitcoinService.class);

    public BitcoinService(@Value("${bitcoin.network}") String networkName,
                          @Value("${bitcoin.forwardingAddress}") String forwardingAddressHash,
                          TransactionRepository transactionRepository) {

        this.pendingTransactionMap = new HashMap<>();
        this.networkName = networkName;
        this.forwardingAddressHash = forwardingAddressHash;
        this.transactionRepository = transactionRepository;

        BriefLogFormatter.init();

        // Setting bitcoin network
        if (networkName.equals("testnet")) {
            params = TestNet3Params.get();
            filePrefix = "forwarding-service-testnet";
        } else if (networkName.equals("regtest")) {
            params = RegTestParams.get();
            filePrefix = "forwarding-service-regtest";

        } else {
            params = MainNetParams.get();
            filePrefix = "forwarding-service";
        }

        initWalletAppKit();
        initCoinReceivedListener();

    }

    /**
     * Finds a transaction matching a payment output address and satoshis value within a certain
     * error margin.
     * @param payment
     * @return Matching Transaction
     */
    public Transaction findTransaction(Payment payment) {

        Transaction tx = getPendingTransaction(payment.getAddress());

        for (TransactionOutput txOut : tx.getOutputs()) {
            if (isEquivalentWithMargin(payment.getSatoshis(), txOut.getValue().value, EQUIVALENCY_MARGIN)) {
                 return tx;
            }
        }
        return null;
    }

    /**
     * Initialize a WalletAppKit a wrapper that contains the Wallet, Blockchain, Blockstore and PeerGroup.
     * Will download a copy of the blockchain, which might take a while.
     */

    private void initWalletAppKit() {

        // Start up a basic app using a class that automates some boilerplate. Ensure we always have at least one key.
        kit = new WalletAppKit(params, new File("."), filePrefix) {
            @Override
            protected void onSetupCompleted() {
                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
                if (wallet().getKeyChainGroupSize() < 1)
                    wallet().importKey(new ECKey());
            }
        };

        if (params == RegTestParams.get()) {
            // Regression test mode is designed for testing and development only, so there's no public network for it.
            // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
            kit.connectToLocalHost();
        }

        // Download the block chain and wait until it's done.
        kit.startAsync();
        kit.awaitRunning();
    }

    /**
     * Evaluate that two amounts are equivalent under a certain margin
     * @param arg0 satoshis
     * @param arg1 satoshis
     * @param margin
     * @return
     */
    private boolean isEquivalentWithMargin(long arg0, long arg1, double margin) {
        double difference = (double) (arg0 - arg1) / arg0;
        if (difference > margin || difference < -margin) {
            return false;
        }
        return true;
    }

    /**
     * Get a new Bitcoin address to send payment. Will provide a new address
     * everytime it's called.
     * @return fresh Address
     */
    public String getReceiveAddress() {
        return kit.wallet().freshReceiveAddress().toString();
    }

    public WalletAppKit getKit() {
        return kit;
    }

    public NetworkParameters getParams() {
        return params;
    }

    public void setParams(NetworkParameters params) {
        this.params = params;
    }

    public void initCoinReceivedListener() {
        kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                for (TransactionOutput txOut : tx.getOutputs()) {
                    if (txOut.isMine(wallet)) {
                        String key = txOut.getScriptPubKey().getToAddress(params).toString();
                        // transactionRepository.save(tx);
                        pendingTransactionMap.put(key, tx);
                        LOG.debug("Transaction Output received Address : {} Value: {}", key, txOut.getValue().getValue());
                        LOG.info("Funds has been received, added to pending transactions");
                    }

                }
            }
        });
    }

    private Map<String, Transaction> getPendingTransactionMap() {
        synchronized (pendingTransactionMap) {
            return pendingTransactionMap;
        }
    }

    public Transaction getPendingTransaction(String address) {
        synchronized (pendingTransactionMap) {
            return pendingTransactionMap.get(address);
        }
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getForwardingAddressHash() {
        return forwardingAddressHash;
    }

    private void setForwardingAddressHash(String forwardingAddressHash) {
        this.forwardingAddressHash = forwardingAddressHash;
    }
}
