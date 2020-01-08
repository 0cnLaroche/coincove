package webserver.model;

public enum PaymentStatus {

    CANCELLED,
    ERROR,
    /**
     * Payment has been matched to a blockchain transaction, but is pending required number of
     * confirmations in order to be validated.
     */
    PENDING_CONFIRMATION,
    /**
     * Payment information has been received, but has not been matched to a blockchain
     * transaction yet.
     */
    PENDING_MATCH,
    /**
     * Payment is in process by the system. Transitional state
     */
    PROCESSING,
    REFUSED,
    /**
     * The payment has received the minimum number of confirmations and is accepted by the vendor.
     */
    VERIFIED,
    /**
     * Transaction amount doesn't match the order
     */
    WRONG_AMOUNT

}
