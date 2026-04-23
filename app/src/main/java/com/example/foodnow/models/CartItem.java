package com.example.foodnow.models;

/** Món trong giỏ hàng (local-only, không lưu Firestore). */
public class CartItem {
    private String foodId;
    private String title;
    private long price;
    private String imageUrl;
    private String storeId;
    private String storeName;
    private long deliveryFee;
    private int quantity;

    public CartItem() {}

    public CartItem(String foodId, String title, long price, String imageUrl,
                    String storeId, String storeName, long deliveryFee, int quantity) {
        this.foodId = foodId;
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl;
        this.storeId = storeId;
        this.storeName = storeName;
        this.deliveryFee = deliveryFee;
        this.quantity = quantity;
    }

    public String getFoodId() { return foodId; }
    public String getTitle() { return title; }
    public long getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public long getDeliveryFee() { return deliveryFee; }
    public int getQuantity() { return quantity; }
    public long getSubtotal() { return price * quantity; }

    public void setFoodId(String foodId) { this.foodId = foodId; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(long price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public void setDeliveryFee(long deliveryFee) { this.deliveryFee = deliveryFee; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
