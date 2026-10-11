package com.example.vnpay.model;

public final class CartItem implements java.io.Serializable {
    @java.io.Serial private static final long serialVersionUID = 1L;
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        setQuantity(quantity);
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public long getAmountVnd() { return Math.multiplyExact(product.getPriceVnd(), quantity); }

    public void setQuantity(int quantity) {
        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("Quantity must be between 1 and 99.");
        }
        this.quantity = quantity;
    }
}
