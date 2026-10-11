package com.example.vnpay.payment;

import java.time.Instant;

public final class Order {
    public enum Status { PENDING, PAID, FAILED }

    private final String reference;
    private final long amountVnd;
    private final Instant createdAt;
    private Status status = Status.PENDING;
    private String transactionNo;

    Order(String reference, long amountVnd, Instant createdAt) {
        this.reference = reference;
        this.amountVnd = amountVnd;
        this.createdAt = createdAt;
    }

    public String getReference() { return reference; }
    public long getAmountVnd() { return amountVnd; }
    public Instant getCreatedAt() { return createdAt; }
    public synchronized Status getStatus() { return status; }
    public synchronized String getTransactionNo() { return transactionNo; }

    public synchronized boolean finish(Status newStatus, String newTransactionNo) {
        if (status != Status.PENDING) return false;
        if (newStatus == Status.PENDING) throw new IllegalArgumentException("Final status required");
        status = newStatus;
        transactionNo = newTransactionNo;
        return true;
    }
}
