package com.example.foodnow.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class Order {

    private String id;
    private String userId;
    private String storeId;
    private String storeName;
    private String address;
    private String paymentMethod;
    private String note;
    private long subtotal;
    private long deliveryFee;
    private long total;
    private String status;
    private Timestamp createdAt;
    private List<OrderItem> items;

    // Constructor rỗng — bắt buộc cho Firestore
    public Order() {}

    // Getter
    public String          getId()            { return id; }
    public String          getUserId()        { return userId; }
    public String          getStoreId()       { return storeId; }
    public String          getStoreName()     { return storeName; }
    public String          getAddress()       { return address; }
    public String          getPaymentMethod() { return paymentMethod; }
    public String          getNote()          { return note; }
    public long            getSubtotal()      { return subtotal; }
    public long            getDeliveryFee()   { return deliveryFee; }
    public long            getTotal()         { return total; }
    public String          getStatus()        { return status; }
    public Timestamp       getCreatedAt()     { return createdAt; }
    public List<OrderItem> getItems()         { return items; }

    // Setter
    public void setId(String id)                       { this.id = id; }
    public void setUserId(String userId)               { this.userId = userId; }
    public void setStoreId(String storeId)             { this.storeId = storeId; }
    public void setStoreName(String storeName)         { this.storeName = storeName; }
    public void setAddress(String address)             { this.address = address; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setNote(String note)                   { this.note = note; }
    public void setSubtotal(long subtotal)             { this.subtotal = subtotal; }
    public void setDeliveryFee(long deliveryFee)       { this.deliveryFee = deliveryFee; }
    public void setTotal(long total)                   { this.total = total; }
    public void setStatus(String status)               { this.status = status; }
    public void setCreatedAt(Timestamp createdAt)      { this.createdAt = createdAt; }
    public void setItems(List<OrderItem> items)        { this.items = items; }

    /**
     * Một món trong đơn hàng (embedded object trong Firestore)
     */
    public static class OrderItem {
        private String foodId;
        private String title;
        private long price;
        private int quantity;
        private String imageUrl;

        public OrderItem() {}

        public OrderItem(String foodId, String title, long price,
                         int quantity, String imageUrl) {
            this.foodId   = foodId;
            this.title    = title;
            this.price    = price;
            this.quantity = quantity;
            this.imageUrl = imageUrl;
        }

        public String getFoodId()   { return foodId; }
        public String getTitle()    { return title; }
        public long   getPrice()    { return price; }
        public int    getQuantity() { return quantity; }
        public String getImageUrl() { return imageUrl; }

        public void setFoodId(String foodId)     { this.foodId = foodId; }
        public void setTitle(String title)       { this.title = title; }
        public void setPrice(long price)         { this.price = price; }
        public void setQuantity(int quantity)    { this.quantity = quantity; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}
