package webserver.service;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class BitcoinServiceTest {

    private static BitcoinService sut;

    @BeforeClass
    public static void init() {
        sut = new BitcoinService("regtest", "?????");
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
