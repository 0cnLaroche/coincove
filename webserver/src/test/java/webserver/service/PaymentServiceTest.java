package webserver.service;

import org.bitcoinj.core.*;
import org.junit.Ignore;
import org.mockito.ArgumentMatchers;
import org.mockito.MockitoAnnotations;
import webserver.confidence.ConfidenceValidator;
import webserver.model.Payment;
import webserver.model.PaymentStatus;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
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
    public void receivePaymentTest() {

        // TODO: Mock LocalDateTime

        Transaction tx = new Transaction(bitcoinService.getParams());

        tx.addOutput(Coin.valueOf(VALID_SATOSHIS), VALID_ADDRESS);
        tx.getConfidence(new Context(bitcoinService.getParams())).setDepthInBlocks(0);

        VALID_PAYMENT.setId("1111");

        //when(bitcoinService.getPendingTransaction(VALID_ADDRESS.toString())).thenReturn(tx);
        when(bitcoinService.findTransaction(ArgumentMatchers.any(Payment.class))).thenReturn(tx);
        when(paymentRepository.save(ArgumentMatchers.any(Payment.class))).thenReturn(VALID_PAYMENT);
        when(confidenceValidator.confirmationsNeeded(ArgumentMatchers.anyDouble())).thenReturn(2);

        paymentService.receive(VALID_PAYMENT);

        tx.getConfidence(new Context(bitcoinService.getParams())).setConfidenceType(TransactionConfidence.ConfidenceType.BUILDING);
        tx.getConfidence(new Context(bitcoinService.getParams())).setDepthInBlocks(1);
        tx.getConfidence(new Context(bitcoinService.getParams())).setDepthInBlocks(2);
        tx.getConfidence(new Context(bitcoinService.getParams())).queueListeners(TransactionConfidence.Listener.ChangeReason.DEPTH);

        // Processing is async , wait a bit until it's being processed.  If still active after a minute,
        // something is wrong and test should fail.
        LocalDateTime timeout = LocalDateTime.now().plusMinutes(1);

        while(paymentService.getPaymentsInProcessCount() != 0 && LocalDateTime.now().isBefore(timeout));

        assertEquals("Status of payment has been updated", PaymentStatus.VERIFIED,
                VALID_PAYMENT.getStatus());
        assertEquals(2, VALID_PAYMENT.getConfirmationsReceived());
        assertNotNull(VALID_PAYMENT.getStatusTime());
        assertTrue(paymentService.getValidatedPaymentQueue().contains(VALID_PAYMENT));

        // TODO: check that order is being processed

    }
}