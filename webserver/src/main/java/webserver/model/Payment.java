package webserver.model;

import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    @Id
    private String id;
    private BigDecimal value;
    private String currency;
    private String address;
    private long satoshis;
    private int confirmationsRequired;
    private int confirmationsReceived;
    private LocalDateTime submittedTime;
    private LocalDateTime processedTime;
    private LocalDateTime statusTime;
    private PaymentStatus status;
    private String transactionId;
    private String orderId;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getSatoshis() {
        return satoshis;
    }

    public void setSatoshis(long satoshis) {
        this.satoshis = satoshis;
    }

    public LocalDateTime getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(LocalDateTime submittedTime) {
        this.submittedTime = submittedTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(LocalDateTime processedTime) {
        this.processedTime = processedTime;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getConfirmationsRequired() {
        return confirmationsRequired;
    }

    public void setConfirmationsRequired(int confirmationsRequired) {
        this.confirmationsRequired = confirmationsRequired;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(LocalDateTime statusTime) {
        this.statusTime = statusTime;
    }

    public int getConfirmationsReceived() {
        return confirmationsReceived;
    }

    public void setConfirmationsReceived(int confirmationsReceived) {
        this.confirmationsReceived = confirmationsReceived;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Payment) {
            if (((Payment) o).id.equals(this.id)) {
                return true;
            }
        }
        return false;
    }
}
