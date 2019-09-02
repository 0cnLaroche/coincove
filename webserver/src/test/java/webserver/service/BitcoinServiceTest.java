package webserver.service;

import org.junit.BeforeClass;
import org.junit.Test;

public class BitcoinServiceTest {

    private static BitcoinService sut;

    @BeforeClass
    public static void init() {
        sut = new BitcoinService("testnet", "?????");
    }

    @Test
    public void getReceiveAddressTest() {
        String address = sut.getReceiveAddress();
        assert (address.charAt(0) == 'm' || address.charAt(0) == 'n');
    }
}
