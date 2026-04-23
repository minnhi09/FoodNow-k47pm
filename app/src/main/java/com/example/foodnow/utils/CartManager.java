package com.example.foodnow.utils;

import com.example.foodnow.models.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> items = new ArrayList<>();
    private String currentStoreId;
    private String currentStoreName;

    private CartManager() {
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public synchronized boolean isFromDifferentStore(String storeId) {
        return currentStoreId != null && !currentStoreId.equals(storeId);
    }

    public synchronized void clearCart() {
        items.clear();
        currentStoreId = null;
        currentStoreName = null;
    }

    public synchronized void addItem(CartItem item) {
        if (item == null) {
            return;
        }

        if (isFromDifferentStore(item.getStoreId())) {
            clearCart();
        }

        if (items.isEmpty()) {
            currentStoreId = item.getStoreId();
            currentStoreName = item.getStoreName();
        }

        for (CartItem existingItem : items) {
            if (Objects.equals(existingItem.getFoodId(), item.getFoodId())) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                return;
            }
        }

        items.add(item);
    }

    public synchronized void removeItem(String foodId) {
        for (int i = 0; i < items.size(); i++) {
            CartItem existingItem = items.get(i);
            if (Objects.equals(existingItem.getFoodId(), foodId)) {
                if (existingItem.getQuantity() > 1) {
                    existingItem.setQuantity(existingItem.getQuantity() - 1);
                } else {
                    items.remove(i);
                }
                break;
            }
        }

        if (items.isEmpty()) {
            clearCart();
        }
    }

    public synchronized List<CartItem> getItems() {
        return items;
    }

    public synchronized long getSubtotal() {
        long total = 0L;
        for (CartItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public synchronized int getItemCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public synchronized String getCurrentStoreId() {
        return currentStoreId;
    }

    public synchronized String getCurrentStoreName() {
        return currentStoreName;
    }
}
