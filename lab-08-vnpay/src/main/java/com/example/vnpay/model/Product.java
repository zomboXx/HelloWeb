package com.example.vnpay.model;

public final class Product implements java.io.Serializable {
    @java.io.Serial private static final long serialVersionUID = 1L;
    private final String code;
    private final String description;
    private final long priceVnd;

    public Product(String code, String description, long priceVnd) {
        this.code = code;
        this.description = description;
        this.priceVnd = priceVnd;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
    public long getPriceVnd() { return priceVnd; }
}
