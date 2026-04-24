package com.example.foodnow.utils;

import com.example.foodnow.models.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartManager {
    public interface OnCartChangedListener {
        void onCartChanged();
    }

    private static CartManager instance;
    private final List<CartItem> items = new ArrayList<>();
    private final List<OnCartChangedListener> listeners = new ArrayList<>();
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
        notifyChanged();
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
                existingItem.setQuantity(existingItem.getQuantity() + 1);
                notifyChanged();
                return;
            }
        }

        // Nếu là món mới, đảm bảo số lượng ban đầu là 1
        item.setQuantity(1);
        items.add(item);
        notifyChanged();
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
        } else {
            notifyChanged();
        }
    }

    public synchronized void removeItemFully(String foodId) {
        items.removeIf(item -> Objects.equals(item.getFoodId(), foodId));
        if (items.isEmpty()) {
            clearCart();
        } else {
            notifyChanged();
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

    public synchronized void registerListener(OnCartChangedListener listener) {
        if (listener == null || listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    public synchronized void unregisterListener(OnCartChangedListener listener) {
        listeners.remove(listener);
    }

    private void notifyChanged() {
        List<OnCartChangedListener> snapshot = new ArrayList<>(listeners);
        for (OnCartChangedListener listener : snapshot) {
            listener.onCartChanged();
        }
    }
}
