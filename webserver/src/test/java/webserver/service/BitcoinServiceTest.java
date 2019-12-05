package webserver.service;

import org.bitcoinj.core.Transaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import webserver.repository.TransactionRepository;

import java.io.IOException;

public class BitcoinServiceTest {

    private static BitcoinService sut;

    @BeforeClass
    public static void init() throws IOException, InterruptedException {
        //System.err.println("commence");
        //Runtime.getRuntime().exec("bitcoind -regtest -daemon").waitFor();
        //System.err.println("bitcoind started in regtest mode");
        TransactionRepository transactionRepository = Mockito.mock(TransactionRepository.class);
        sut = new BitcoinService("testnet", "?????", transactionRepository);
    }

    @AfterClass
    public static void end() throws IOException, InterruptedException {
        //Runtime.getRuntime().exec("bitcoin-cli stop").waitFor();
        //System.err.println("bitcoind stopped");
    }

    @Test
    public void getReceiveAddressTest() {
        String address = sut.getReceiveAddress();
        assert (address.charAt(0) == 'm' || address.charAt(0) == 'n');
    }

    @Test
    @Ignore
    public void testOnCoinReceived() {
        assert sut.getKit().wallet().getBalance().getValue() > 0;
    }
}
