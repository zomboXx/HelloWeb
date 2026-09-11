package com.example.cart.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String code;
    private final String description;
    private final BigDecimal price;

    public Product(String code, String description, BigDecimal price) {
        this.code = code;
        this.description = description;
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
