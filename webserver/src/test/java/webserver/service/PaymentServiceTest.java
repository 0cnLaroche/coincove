package webserver.service;

import webserver.model.Payment;
import webserver.model.PaymentStatus;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.javamoney.moneta.FastMoney;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import webserver.repository.PaymentRepository;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;
    @Mock
    private BitcoinService bitcoinService;
    @Mock
    private PaymentRepository paymentRepository;

    private static final Payment VALID_PAYMENT = new Payment();
    private static final ECKey VALID_ECKEY = new ECKey();
    private static Address VALID_ADDRESS;
    private static final long VALID_SATOSHIS = 100000;
    private static final long INVALID_SATOSHIS = 999;
    private static final FastMoney VALID_VALUE = FastMoney.of(100.0, "CAD");

    @Before
    public void setUp() throws Exception {
        //MockitoAnnotations.initMocks(this);
        when(bitcoinService.getParams()).thenReturn(RegTestParams.get());
        VALID_ADDRESS = Address.fromKey(bitcoinService.getParams(), VALID_ECKEY,  Script.ScriptType.P2PKH);
        VALID_PAYMENT.setAddress(VALID_ADDRESS.toString());
        VALID_PAYMENT.setSatoshis(VALID_SATOSHIS);

    }

    @Test
    public void verifyValidPaymentTest() {
        Transaction tx = new Transaction(bitcoinService.getParams());

        tx.addOutput(Coin.valueOf(VALID_SATOSHIS), VALID_ADDRESS);

        when(bitcoinService.getTransaction(VALID_ADDRESS.toString())).thenReturn(tx);

        // Case : Valid payment has status Verified
        assertEquals(PaymentStatus.VERIFIED, paymentService.verifyPayment(VALID_PAYMENT));

    }

    @Test
    public void verifyPaymentNotReceivedTest() {

        // Case : Payment for transaction not received in the wallet returns status
        // not received.
        assertEquals(PaymentStatus.PENDING,
                paymentService.verifyPayment(VALID_PAYMENT));

    }

    @Test
    public void verifyWrongAmountPaymentTest() {

        Transaction tx = new Transaction(bitcoinService.getParams());

        tx.addOutput(Coin.valueOf(INVALID_SATOSHIS), VALID_ADDRESS);

        when(bitcoinService.getTransaction(VALID_ADDRESS.toString())).thenReturn(tx);

        // Case : Valid payment has status Verified
        assertEquals(PaymentStatus.WRONG_AMOUNT, paymentService.verifyPayment(VALID_PAYMENT));

    }

    @Test
    public void receivePaymentTest() {

        // TODO: Mock LocalDateTime

        Transaction tx = new Transaction(bitcoinService.getParams());

        tx.addOutput(Coin.valueOf(VALID_SATOSHIS), VALID_ADDRESS);

        when(bitcoinService.getTransaction(VALID_ADDRESS.toString())).thenReturn(tx);
        when(paymentRepository.save(VALID_PAYMENT)).thenReturn(VALID_PAYMENT);

        paymentService.receive(VALID_PAYMENT);

        // Processing is async , wait a bit until it's being processed.  If still active after a minute,
        // something is wrong and test should fail.
        LocalDateTime timeout = LocalDateTime.now().plusMinutes(1);

        while(paymentService.getPaymentsInProcessCount() != 0 && LocalDateTime.now().isBefore(timeout));

        assertEquals("Status of payment has been updated", PaymentStatus.VERIFIED,
                VALID_PAYMENT.getStatus());
        assertTrue(paymentService.getValidatedPaymentQueue().contains(VALID_PAYMENT));

        // TODO: check that order is being processed

    }
}