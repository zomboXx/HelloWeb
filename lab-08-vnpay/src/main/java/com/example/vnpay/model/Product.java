package com.example.vnpay.model;

public final class Product implements java.io.Serializable {
    @java.io.Serial private static final long serialVersionUID = 1L;
    private final String code;
    private final String artist;
    private final String title;
    private final long priceVnd;

    public Product(String code, String artist, String title, long priceVnd) {
        this.code = code;
        this.artist = artist;
        this.title = title;
        this.priceVnd = priceVnd;
    }

    public String getCode() { return code; }
    public String getArtist() { return artist; }
    public String getTitle() { return title; }
    public String getDescription() { return artist + " - " + title; }
    public String getCoverPath() { return "/images/albums/" + code + ".svg"; }
    public long getPriceVnd() { return priceVnd; }
}
