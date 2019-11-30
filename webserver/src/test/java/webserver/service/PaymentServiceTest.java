package webserver.service;

import javafx.animation.PauseTransition;
import org.bitcoinj.core.*;
import org.junit.Ignore;
import org.mockito.ArgumentMatchers;
import org.mockito.MockitoAnnotations;
import webserver.confidence.ConfidenceValidator;
import webserver.model.Payment;
import webserver.model.PaymentStatus;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.javamoney.moneta.FastMoney;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import webserver.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

//@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;
    @Mock
    private BitcoinService bitcoinService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ConfidenceValidator confidenceValidator;

    private static final Payment VALID_PAYMENT = new Payment();
    private static final ECKey VALID_ECKEY = new ECKey();
    private static Address VALID_ADDRESS;
    private static final BigDecimal VALID_VALUE = BigDecimal.valueOf(100.0);
    private static final long VALID_SATOSHIS = 100000;
    private static final long INVALID_SATOSHIS = 999;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(bitcoinService.getParams()).thenReturn(RegTestParams.get());
        VALID_ADDRESS = Address.fromKey(RegTestParams.get(), VALID_ECKEY,  Script.ScriptType.P2PKH);
        VALID_PAYMENT.setAddress(VALID_ADDRESS.toString());
        VALID_PAYMENT.setSatoshis(VALID_SATOSHIS);
        VALID_PAYMENT.setValue(VALID_VALUE);
        VALID_PAYMENT.setId(null);

    }

    @Test
    @Ignore
    public void verifyValidPaymentTest() {
        Transaction tx = new Transaction(bitcoinService.getParams());

        tx.addOutput(Coin.valueOf(VALID_SATOSHIS), VALID_ADDRESS);

        when(bitcoinService.getPendingTransaction(VALID_ADDRESS.toString())).thenReturn(tx);

        // Case : Valid payment has status Verified
        assertEquals(PaymentStatus.VERIFIED, paymentService.verifyPayment(VALID_PAYMENT));

    }

    @Test
    @Ignore
    public void verifyReceivedPaymentWithMarginTest() {
        Transaction tx = new Transaction(bitcoinService.getParams());
        Payment payment = new Payment();
        payment.setAddress(VALID_ADDRESS.toString());

        long compared = Math.round(((double) VALID_SATOSHIS )* 1.004);
        payment.setSatoshis(compared);

        tx.addOutput(Coin.valueOf(VALID_SATOSHIS), VALID_ADDRESS);

        when(bitcoinService.getPendingTransaction(ArgumentMatchers.eq(VALID_ADDRESS.toString()))).thenReturn(tx);

        // Case : Valid payment is not equal to transaction but equivalent under set margin
        assertEquals(PaymentStatus.RECEIVED, paymentService.verifyPayment(payment));

        compared = Math.round(((double) VALID_SATOSHIS ) * 1.2);
        payment.setSatoshis(compared);

        // Case : Payment is not equivalent cause over margin
        assertNotEquals(PaymentStatus.RECEIVED, paymentService.verifyPayment(payment));

    }

    @Test
    @Ignore
    public void verifyPaymentNotReceivedTest() {

        // Case : Payment for transaction not received in the wallet returns status
        // not received.
        assertEquals(PaymentStatus.PENDING,
                paymentService.verifyPayment(VALID_PAYMENT));

    }

    @Test
    @Ignore
    public void verifyWrongAmountPaymentTest() {

        Transaction tx = new Transaction(bitcoinService.getParams());

        tx.addOutput(Coin.valueOf(INVALID_SATOSHIS), VALID_ADDRESS);

        when(bitcoinService.getPendingTransaction(VALID_ADDRESS.toString())).thenReturn(tx);

        // Case : Valid payment has status Verified
        assertEquals(PaymentStatus.WRONG_AMOUNT, paymentService.verifyPayment(VALID_PAYMENT));

    }

    @Test
    public void receivePaymentTest() {

        // TODO: Mock LocalDateTime

        Transaction tx = new Transaction(bitcoinService.getParams());

        tx.addOutput(Coin.valueOf(VALID_SATOSHIS), VALID_ADDRESS);
        tx.getConfidence(new Context(bitcoinService.getParams())).setDepthInBlocks(100);

        VALID_PAYMENT.setId("1111");

        //when(bitcoinService.getPendingTransaction(VALID_ADDRESS.toString())).thenReturn(tx);
        when(bitcoinService.findTransaction(ArgumentMatchers.any(Payment.class))).thenReturn(tx);
        when(paymentRepository.save(ArgumentMatchers.any(Payment.class))).thenReturn(VALID_PAYMENT);
        when(confidenceValidator.confirmationsNeeded(ArgumentMatchers.anyDouble())).thenReturn(2);

        paymentService.receive(VALID_PAYMENT);

        // Processing is async , wait a bit until it's being processed.  If still active after a minute,
        // something is wrong and test should fail.
        LocalDateTime timeout = LocalDateTime.now().plusMinutes(1);

        while(paymentService.getPaymentsInProcessCount() != 0 && LocalDateTime.now().isBefore(timeout));

        assertEquals("Status of payment has been updated", PaymentStatus.VERIFIED,
                VALID_PAYMENT.getStatus());
        assertNotNull(VALID_PAYMENT.getStatusTime());
        assertTrue(paymentService.getValidatedPaymentQueue().contains(VALID_PAYMENT));

        // TODO: check that order is being processed

    }
}