package com.example.vnpay.payment;

import com.example.vnpay.model.CartItem;
import com.example.vnpay.model.Product;

/** Immutable snapshot of a CD and its quantity when payment begins. */
public final class OrderLine {
    private final Product product;
    private final int quantity;

    public OrderLine(CartItem item) {
        this.product = item.getProduct();
        this.quantity = item.getQuantity();
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public long getAmountVnd() { return Math.multiplyExact(product.getPriceVnd(), quantity); }
}
