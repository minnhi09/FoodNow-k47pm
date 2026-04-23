package com.example.foodnow.models;

import com.google.firebase.Timestamp;

import java.util.List;

/**
 * Đơn hàng — khớp với collection Orders trong Firestore.
 * Status flow: "Đơn mới" → "Đang làm" → "Sẵn sàng" → "Đang giao" → "Hoàn thành"
 * Hoặc: "Đơn mới" → "Đã hủy"
 */
public class Order {

    // Trạng thái đơn hàng
    public static final String STATUS_NEW        = "Đơn mới";
    public static final String STATUS_PROCESSING = "Đang làm";
    public static final String STATUS_READY      = "Sẵn sàng";
    public static final String STATUS_DELIVERING = "Đang giao";
    public static final String STATUS_DONE       = "Hoàn thành";
    public static final String STATUS_CANCELLED  = "Đã hủy";

    private String        id;            // document ID (set thủ công sau khi đọc)
    private String        userId;
    private String        storeId;
    private String        storeName;
    private String        customerName;  // tên khách hàng (đọc từ Users khi cần)
    private String        address;
    private String        paymentMethod;
    private String        note;
    private double        subtotal;
    private double        deliveryFee;
    private double        total;
    private String        status;
    private Timestamp     createdAt;
    private List<OrderItem> items;

    // Firestore yêu cầu constructor rỗng
    public Order() {}

    // ─── Getters ────────────────────────────────────────────

    public String          getId()            { return id; }
    public String          getUserId()        { return userId; }
    public String          getStoreId()       { return storeId; }
    public String          getStoreName()     { return storeName; }
    public String          getCustomerName()  { return customerName; }
    public String          getAddress()       { return address; }
    public String          getPaymentMethod() { return paymentMethod; }
    public String          getNote()          { return note; }
    public double          getSubtotal()      { return subtotal; }
    public double          getDeliveryFee()   { return deliveryFee; }
    public double          getTotal()         { return total; }
    public String          getStatus()        { return status; }
    public Timestamp       getCreatedAt()     { return createdAt; }
    public List<OrderItem> getItems()         { return items; }

    // ─── Setters ────────────────────────────────────────────

    public void setId(String id)                       { this.id            = id; }
    public void setUserId(String userId)               { this.userId        = userId; }
    public void setStoreId(String storeId)             { this.storeId       = storeId; }
    public void setStoreName(String storeName)         { this.storeName     = storeName; }
    public void setCustomerName(String customerName)   { this.customerName  = customerName; }
    public void setAddress(String address)             { this.address       = address; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setNote(String note)                   { this.note          = note; }
    public void setSubtotal(double subtotal)           { this.subtotal      = subtotal; }
    public void setDeliveryFee(double deliveryFee)     { this.deliveryFee   = deliveryFee; }
    public void setTotal(double total)                 { this.total         = total; }
    public void setStatus(String status)               { this.status        = status; }
    public void setCreatedAt(Timestamp createdAt)      { this.createdAt     = createdAt; }
    public void setItems(List<OrderItem> items)        { this.items         = items; }

    /** Tóm tắt các món: "Phở bò x2, Bún bò x1" */
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
