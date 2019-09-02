package webserver.service;

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

    Logger log = LoggerFactory.getLogger(PaymentService.class);

    private static final int PROCESSING_DEFAULT_TIMEOUT = 120;
    private static final int MAX_WORKERS = 1000;

    private static List<Payment> pendingPaymentList;

    private static List<Payment> rejectedPaymentList;

    private static BlockingQueue<Payment> validatedPaymentQueue;

    @Autowired
    public PaymentService(BitcoinService bitcoinService, PaymentRepository paymentRepository) {
        this.bitcoinService = bitcoinService;
        this.paymentRepository = paymentRepository;
        this.pendingPaymentList = Collections.synchronizedList(new ArrayList<Payment>());
        this.rejectedPaymentList = Collections.synchronizedList(new ArrayList<Payment>());
        this.validatedPaymentQueue = new LinkedBlockingQueue<>();


        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_WORKERS);
    }

    public PaymentStatus verifyPayment(Payment payment) {

        Transaction tx = bitcoinService.getTransaction(payment.getAddress());

        if (tx != null) {
            if (payment.getSatoshis() == tx.getOutput(0).getValue().value) {
                return PaymentStatus.VERIFIED;
            }
            return PaymentStatus.WRONG_AMOUNT;
        }
        return PaymentStatus.PENDING;
    }

    /**
     * Worker that verifies payment right away. Doesn't take one from the pending queue. Shouldn't be called
     * directly by service consumers. Will continue
     * @param payment
     * @param runnable will be run only if a payment is validated
     * @return Payment Status
     */

    private PaymentStatus processPayment(Payment payment, Runnable runnable) {

        log.info("Processing payment");

        LocalDateTime timeout = LocalDateTime.now().plusMinutes(PROCESSING_DEFAULT_TIMEOUT);

        PaymentStatus status = PaymentStatus.PROCESSING;
        payment.setStatus(status);
        paymentRepository.save(payment);

        while(timeout.isAfter(LocalDateTime.now()) && pendingPaymentList.contains(payment)) {

            synchronized (payment) {
                status = verifyPayment(payment);
                payment.setStatus(status);

                switch (status) {
                    case VERIFIED :
                        payment.setStatus(PaymentStatus.VERIFIED);
                        payment.setProcessedTime(LocalDateTime.now());
                        pendingPaymentList.remove(payment);
                        validatedPaymentQueue.add(payment);
                        paymentRepository.save(payment);
                        log.info("Payment has been verified");
                        runnable.run();
                        return status;
                    case WRONG_AMOUNT:
                    case REFUSED:
                        payment.setStatus(status);
                        payment.setProcessedTime(LocalDateTime.now());
                        pendingPaymentList.remove(payment);
                        rejectedPaymentList.add(payment);
                        paymentRepository.save(payment);
                        log.info("Payment information doesn't match");
                        return status;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        status = PaymentStatus.REFUSED;
        pendingPaymentList.remove(payment);
        rejectedPaymentList.add(payment);
        synchronized (payment) {
            payment.setProcessedTime(LocalDateTime.now());
            payment.setStatus(status);
            paymentRepository.save(payment);
        }
        log.info("Payment refused");
        return status;
    }

    public Payment receive(Payment payment) {

        payment.setStatus(PaymentStatus.PENDING);
        String freshId = paymentRepository.save(payment).getId();
        payment.setId(freshId);
        pendingPaymentList.add(payment);

        Future<PaymentStatus> future = executor.submit(()->
             processPayment(payment, ()->{})
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

}
