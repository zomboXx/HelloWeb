package com.example.vnpay.model;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Catalog {
    private static final Map<String, Product> PRODUCTS;

    static {
        Map<String, Product> products = new LinkedHashMap<>();
        products.put("8601", new Product("8601", "86 (the band) - True Life Songs and Pictures", 150_000));
        products.put("pf01", new Product("pf01", "Paddlefoot - The first CD", 130_000));
        products.put("pf02", new Product("pf02", "Paddlefoot - The second CD", 150_000));
        products.put("jr01", new Product("jr01", "Joe Rut - Genuine Wood Grained Finish", 150_000));
        PRODUCTS = Map.copyOf(products);
    }

    private Catalog() { }
    public static Product find(String code) { return PRODUCTS.get(code); }
    public static java.util.List<Product> all() {
        return java.util.List.of(PRODUCTS.get("8601"), PRODUCTS.get("pf01"),
                PRODUCTS.get("pf02"), PRODUCTS.get("jr01"));
    }
}
