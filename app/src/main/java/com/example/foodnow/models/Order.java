package com.example.foodnow.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class Order {

    public static final String STATUS_NEW = "Đơn mới";
    public static final String STATUS_PROCESSING = "Đang làm";
    public static final String STATUS_READY = "Sẵn sàng";
    public static final String STATUS_DELIVERING = "Đang giao";
    public static final String STATUS_DONE = "Hoàn thành";
    public static final String STATUS_CANCELLED = "Đã hủy";

    private String id;
    private String userId;
    private String storeId;
    private String storeName;
    private String customerName;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getItemsSummary() {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            if (i > 0) sb.append(", ");
            sb.append(item.getTitle()).append(" x").append(item.getQuantity());
        }
        return sb.toString();
    }
}
