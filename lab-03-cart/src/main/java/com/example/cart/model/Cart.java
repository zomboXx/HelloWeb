package com.example.cart.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Cart implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, CartItem> items = new LinkedHashMap<>();

    public synchronized void add(Product product) {
        CartItem existingItem = items.get(product.getCode());
        if (existingItem == null) {
            items.put(product.getCode(), new CartItem(product, 1));
        } else {
            existingItem.incrementQuantity();
        }
    }

    public synchronized void update(String productCode, int quantity) {
        CartItem item = items.get(productCode);
        if (item != null) {
            item.setQuantity(quantity);
        }
    }

    public synchronized void remove(String productCode) {
        items.remove(productCode);
    }

    public synchronized List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public synchronized boolean isEmpty() {
        return items.isEmpty();
    }

    public synchronized int getItemCount() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public synchronized BigDecimal getTotal() {
        return items.values().stream()
                .map(CartItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
