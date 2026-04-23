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

    public Order() {
    }

    public Order(String id, String userId, String storeId, String storeName, String address,
                 String paymentMethod, String note, long subtotal, long deliveryFee, long total,
                 String status, Timestamp createdAt, List<OrderItem> items) {
        this.id = id;
        this.userId = userId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.note = note;
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(long subtotal) {
        this.subtotal = subtotal;
    }

    public long getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(long deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public static class OrderItem {
        private String foodId;
        private String title;
        private long price;
        private int quantity;
        private String imageUrl;

        public OrderItem() {
        }

        public OrderItem(String foodId, String title, long price, int quantity, String imageUrl) {
            this.foodId = foodId;
            this.title = title;
            this.price = price;
            this.quantity = quantity;
            this.imageUrl = imageUrl;
        }

        public String getFoodId() {
            return foodId;
        }

        public void setFoodId(String foodId) {
            this.foodId = foodId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getPrice() {
            return price;
        }

        public void setPrice(long price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
