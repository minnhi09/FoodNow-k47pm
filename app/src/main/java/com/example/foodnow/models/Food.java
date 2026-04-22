package com.example.foodnow.models;

public class Food {

    private String id;
    private String title;
    private String description;
    private long price;
    private String imageUrl;
    private float rating;
    private String storeId;
    private String categoryId;
    private boolean isAvailable;

    // ⚠️ BẮT BUỘC — Firestore cần constructor rỗng để deserialize
    public Food() {}

    public Food(String id, String title, String description, long price,
                String imageUrl, float rating, String storeId,
                String categoryId, boolean isAvailable) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.storeId = storeId;
        this.categoryId = categoryId;
        this.isAvailable = isAvailable;
    }

    // Getter
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public float getRating() { return rating; }
    public String getStoreId() { return storeId; }
    public String getCategoryId() { return categoryId; }
    public boolean isAvailable() { return isAvailable; }

    // Setter
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(long price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setRating(float rating) { this.rating = rating; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
