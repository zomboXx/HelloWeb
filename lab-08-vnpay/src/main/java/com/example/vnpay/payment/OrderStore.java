package com.example.vnpay.payment;

import com.example.vnpay.model.Cart;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Demo-only storage. Replace with a database before using real payments. */
public final class OrderStore {
    private static final OrderStore INSTANCE = new OrderStore();
    private final ConcurrentMap<String, Order> orders = new ConcurrentHashMap<>();

    private OrderStore() { }
    public static OrderStore instance() { return INSTANCE; }

    public Order create(Cart cart) {
        synchronized (cart) {
            long amountVnd = cart.getTotalVnd();
            if (amountVnd <= 0 || amountVnd > 9_999_999_999L) {
                throw new IllegalArgumentException("Invalid VND amount");
            }
            List<OrderLine> items = cart.getItems().stream().map(OrderLine::new).toList();
            Order order = new Order(UUID.randomUUID().toString().replace("-", ""),
                    amountVnd, Instant.now(), items);
            orders.put(order.getReference(), order);
            return order;
        }
    }

    public Order find(String reference) { return reference == null ? null : orders.get(reference); }
}
