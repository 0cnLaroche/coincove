package webserver.service;

import com.google.common.util.concurrent.ListenableFuture;
import org.bitcoinj.core.TransactionConfidence;
import webserver.confidence.ConfidenceValidator;
import webserver.model.Payment;
import webserver.model.PaymentStatus;
import org.bitcoinj.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

@Service
public class PaymentService {

    private BitcoinService bitcoinService;
    private PaymentRepository paymentRepository;
    private ThreadPoolExecutor executor;
    private ConfidenceValidator confidenceValidator;
    private static List<Payment> pendingPaymentList;
    private static List<Payment> rejectedPaymentList;
    private static BlockingQueue<Payment> validatedPaymentQueue;

    private static final int PROCESSING_DEFAULT_TIMEOUT = 120;
    private static final int MAX_WORKERS = 1000;

    Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    public PaymentService(BitcoinService bitcoinService, PaymentRepository paymentRepository,
                          ConfidenceValidator confidenceValidator) {
        this.bitcoinService = bitcoinService;
        this.paymentRepository = paymentRepository;
        this.confidenceValidator = confidenceValidator;
        //this.pendingPaymentList = Collections.synchronizedList(new ArrayList<Payment>());
        //this.rejectedPaymentList = Collections.synchronizedList(new ArrayList<Payment>());
        //this.validatedPaymentQueue = new LinkedBlockingQueue<>();
        this.pendingPaymentList = new ArrayList<Payment>();
        this.rejectedPaymentList = new ArrayList<Payment>();
        this.validatedPaymentQueue = new LinkedBlockingQueue<>();

        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_WORKERS);
    }

    public PaymentStatus verifyPayment(Payment payment) {
        return null;
    }

    /**
     * Worker that verifies payment right away. Doesn't take one from the pending queue. Shouldn't be called
     * directly by service consumers. Will continue
     * @param payment
     * @param runnable will be run only if a payment is validated
     * @return Payment Status
     */

    private void processPayment(Payment payment, Runnable runnable) {

        log.info("Processing payment");
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setProcessedTime(LocalDateTime.now());

        paymentRepository.save(payment);

        LocalDateTime timeout = LocalDateTime.now().plusMinutes(PROCESSING_DEFAULT_TIMEOUT);

        try {

        while(timeout.isAfter(LocalDateTime.now())) {

            Transaction tx = bitcoinService.findTransaction(payment);

            if (tx != null) {

                payment.setTransactionId(tx.getTxId().toString());
                Future<TransactionConfidence> confidenceFuture =
                        tx.getConfidence().getDepthFuture(payment.getConfirmationsRequired(), executor);
                while (!confidenceFuture.isDone()) {
                    Thread.sleep(100);
                }

                payment.setStatus(PaymentStatus.VERIFIED);
                payment.setStatusTime(LocalDateTime.now());
                pendingPaymentList.remove(payment);
                validatedPaymentQueue.add(payment);
                paymentRepository.save(payment);
                runnable.run();
                // TODO: Process order
                return;
            }

            Thread.sleep(100);

        }

        } catch (InterruptedException e) {

            e.printStackTrace();
        }


        pendingPaymentList.remove(payment);
        rejectedPaymentList.add(payment);
        synchronized (payment) {
            payment.setStatusTime(LocalDateTime.now());
            payment.setStatus(PaymentStatus.REFUSED);
            paymentRepository.save(payment);
        }
        log.info("Payment refused");
        return;
    }

    public Payment receive(Payment payment) {

        payment.setStatus(PaymentStatus.PENDING);
        payment.setConfirmationsRequired(confidenceValidator.confirmationsNeeded(payment.getValue().doubleValue()));
        String freshId = paymentRepository.save(payment).getId();
        payment.setId(freshId);
        pendingPaymentList.add(payment);

        executor.submit(()->
             processPayment(payment, ()->{
                 // Do something once the payment has been verified
             })
        );

        return payment;
    }

    public PaymentStatus paymentStatus(String paymentId) {
        return findById(paymentId).getStatus();
    }

    public Payment findById(String id) {
        return paymentRepository.findById(id).orElse(null);
    }

    public List<Payment> getPendingPaymentList() {
        return pendingPaymentList;
    }

    public Queue<Payment> getValidatedPaymentQueue() {
        return validatedPaymentQueue;
    }

    public List<Payment> getRejectedPaymentList() {
        return this.rejectedPaymentList;
    }

    public int getPaymentsInProcessCount() {
        return executor.getActiveCount();
    }
/*
    private Thread dispatcher = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true) {
                try {
                    Payment p = validatedPaymentQueue.take();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });
*/
}
