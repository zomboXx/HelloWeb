package com.example.vnpay.payment;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Demo-only storage. Replace with a database before using real payments. */
public final class OrderStore {
    private static final OrderStore INSTANCE = new OrderStore();
    private final ConcurrentMap<String, Order> orders = new ConcurrentHashMap<>();

    private OrderStore() { }
    public static OrderStore instance() { return INSTANCE; }

    public Order create(long amountVnd) {
        if (amountVnd <= 0 || amountVnd > 9_999_999_999L) {
            throw new IllegalArgumentException("Invalid VND amount");
        }
        Order order = new Order(UUID.randomUUID().toString().replace("-", ""), amountVnd, Instant.now());
        orders.put(order.getReference(), order);
        return order;
    }

    public Order find(String reference) { return reference == null ? null : orders.get(reference); }
}
