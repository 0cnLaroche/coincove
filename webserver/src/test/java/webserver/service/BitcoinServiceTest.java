package webserver.service;

import org.bitcoinj.core.Transaction;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import webserver.repository.TransactionRepository;

public class BitcoinServiceTest {

    private static BitcoinService sut;

    @BeforeClass
    public static void init() {
        TransactionRepository transactionRepository = Mockito.mock(TransactionRepository.class);
        sut = new BitcoinService("regtest", "?????", transactionRepository);
    }

    @Ignore
    @Test
    public void getReceiveAddressTest() {
        String address = sut.getReceiveAddress();
        assert (address.charAt(0) == 'm' || address.charAt(0) == 'n');
    }

    @Test
    public void testOnCoinReceived() {
        assert sut.getKit().wallet().getBalance().getValue() > 0;
    }
}
