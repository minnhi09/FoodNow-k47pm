package com.example.foodnow.utils;

import com.example.foodnow.models.CartItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton quản lý giỏ hàng — chỉ lưu trong bộ nhớ (không đẩy lên Firestore).
 * Giỏ hàng chỉ chứa món từ 1 quán tại 1 thời điểm.
 */
public class CartManager {

    private static CartManager instance;
    private final List<CartItem> items = new ArrayList<>();
    private String currentStoreId;
    private String currentStoreName;

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    /** Kiểm tra giỏ có món từ quán khác không */
    public boolean isFromDifferentStore(String storeId) {
        return currentStoreId != null && !currentStoreId.equals(storeId) && !items.isEmpty();
    }

    /** Xóa toàn bộ giỏ hàng */
    public void clearCart() {
        items.clear();
        currentStoreId   = null;
        currentStoreName = null;
    }

    /** Thêm món vào giỏ (tăng số lượng nếu đã có) */
    public void addItem(CartItem newItem) {
        // Cập nhật quán hiện tại
        currentStoreId   = newItem.getStoreId();
        currentStoreName = newItem.getStoreName();

        // Kiểm tra đã có trong giỏ chưa
        for (CartItem item : items) {
            if (item.getFoodId().equals(newItem.getFoodId())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                return;
            }
        }
        items.add(newItem);
    }

    /** Giảm số lượng hoặc xóa món */
    public void removeItem(String foodId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getFoodId().equals(foodId)) {
                CartItem item = items.get(i);
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                } else {
                    items.remove(i);
                }
                break;
            }
        }
        if (items.isEmpty()) {
            currentStoreId   = null;
            currentStoreName = null;
        }
    }

    public List<CartItem> getItems()        { return items; }
    public String getCurrentStoreId()       { return currentStoreId; }
    public String getCurrentStoreName()     { return currentStoreName; }
    public int getItemCount()               { return items.size(); }

    /** Tổng tiền tất cả món trong giỏ */
    public long getSubtotal() {
        long total = 0;
        for (CartItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }
}
