package webserver.service;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public BitcoinService(@Value("${bitcoin.network}") String networkName,
                          @Value("${bitcoin.forwardingAddress}") String forwardingAddressHash) {

        this.pendingTransactionMap = new HashMap<>();
        this.networkName = networkName;
        this.forwardingAddressHash = forwardingAddressHash;

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
                String addressStr = tx.getOutput(0).getScriptPubKey().getToAddress(params).toString();
                pendingTransactionMap.put(addressStr, tx);
            }
        });
    }

    public Map<String, Transaction> getPendingTransactionMap() {
        synchronized (pendingTransactionMap) {
            return pendingTransactionMap;
        }
    }

    public Transaction getTransaction(String address) {
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

    public void setForwardingAddressHash(String forwardingAddressHash) {
        this.forwardingAddressHash = forwardingAddressHash;
    }
}
