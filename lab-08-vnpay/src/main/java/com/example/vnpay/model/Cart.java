package com.example.vnpay.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Cart implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private final Map<String, CartItem> items = new LinkedHashMap<>();

    public synchronized void add(Product product) {
        CartItem item = items.get(product.getCode());
        if (item == null) {
            items.put(product.getCode(), new CartItem(product, 1));
        } else if (item.getQuantity() < 99) {
            item.setQuantity(item.getQuantity() + 1);
        }
    }

    public synchronized void update(String code, int quantity) {
        CartItem item = items.get(code);
        if (item != null) item.setQuantity(quantity);
    }

    public synchronized void remove(String code) { items.remove(code); }
    public synchronized boolean isEmpty() { return items.isEmpty(); }
    public synchronized List<CartItem> getItems() { return new ArrayList<>(items.values()); }

    public synchronized long getTotalVnd() {
        long total = 0;
        for (CartItem item : items.values()) total = Math.addExact(total, item.getAmountVnd());
        return total;
    }
}
