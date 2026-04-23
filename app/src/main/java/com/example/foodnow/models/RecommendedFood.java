package com.example.foodnow.models;

public class

RecommendedFood {
    private final String name;
    private final String storeName;
    private final long price;
    private final float rating;
    private final String imageUrl;
    private final boolean popular;

    public RecommendedFood(String name, String storeName, long price, float rating, String imageUrl, boolean popular) {
        this.name = name;
        this.storeName = storeName;
        this.price = price;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.popular = popular;
    }

    public String getName() {
        return name;
    }

    public String getStoreName() {
        return storeName;
    }

    public long getPrice() {
        return price;
    }

    public float getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isPopular() {
        return popular;
    }
}
