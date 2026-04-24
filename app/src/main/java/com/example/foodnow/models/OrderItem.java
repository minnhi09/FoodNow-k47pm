package com.example.foodnow.models;

/** Một dòng trong đơn hàng — khớp với mảng items[] trong Firestore Orders. */
public class OrderItem {

    private String foodId;
    private String title;
    private double price;
    private int    quantity;
    private String imageUrl;

    // Firestore yêu cầu constructor rỗng
    public OrderItem() {}

    public OrderItem(String foodId, String title, double price, int quantity, String imageUrl) {
        this.foodId   = foodId;
        this.title    = title;
        this.price    = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public String getFoodId()   { return foodId; }
    public String getTitle()    { return title; }
    public double getPrice()    { return price; }
    public int    getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }

    public void setFoodId(String foodId)     { this.foodId   = foodId; }
    public void setTitle(String title)       { this.title    = title; }
    public void setPrice(double price)       { this.price    = price; }
    public void setQuantity(int quantity)    { this.quantity = quantity; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /** Tổng tiền của dòng này */
    public double getSubtotal() {
        return price * quantity;
    }
}
