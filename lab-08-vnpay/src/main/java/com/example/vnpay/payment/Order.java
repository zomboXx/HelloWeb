package com.example.vnpay.payment;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class Order {
    public enum Status { PENDING, PAID, FAILED }

    private final String reference;
    private final long amountVnd;
    private final Instant createdAt;
    private final List<OrderLine> items;
    private Status status = Status.PENDING;
    private String transactionNo;

    Order(String reference, long amountVnd, Instant createdAt) {
        this(reference, amountVnd, createdAt, List.of());
    }

    Order(String reference, long amountVnd, Instant createdAt, List<OrderLine> items) {
        this.reference = reference;
        this.amountVnd = amountVnd;
        this.createdAt = createdAt;
        this.items = List.copyOf(items);
    }

    public String getReference() { return reference; }
    public long getAmountVnd() { return amountVnd; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedAtText() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh")).format(createdAt);
    }
    public List<OrderLine> getItems() { return items; }
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
