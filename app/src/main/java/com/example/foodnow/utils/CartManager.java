package com.example.foodnow.utils;

import com.example.foodnow.models.CartItem;
import com.example.foodnow.models.Food;

import java.util.ArrayList;
import java.util.List;

/** Giỏ hàng local-only: chỉ chứa món từ 1 quán tại một thời điểm. */
public class CartManager {
    public enum AddResult {
        ADDED,
        UPDATED,
        STORE_MISMATCH
    }

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    private static CartManager instance;
    private final List<CartItem> items = new ArrayList<>();
    private final List<OnCartChangedListener> listeners = new ArrayList<>();

    private String storeId = "";
    private String storeName = "";
    private long deliveryFee = 0L;

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    public synchronized AddResult addFood(Food food, int quantity, String storeId, String storeName, long deliveryFee) {
        if (food == null || quantity <= 0 || storeId == null || storeId.isEmpty()) {
            return AddResult.STORE_MISMATCH;
        }

        if (!items.isEmpty() && this.storeId != null && !this.storeId.isEmpty()
                && !this.storeId.equals(storeId)) {
            return AddResult.STORE_MISMATCH;
        }

        this.storeId = storeId;
        this.storeName = storeName != null ? storeName : "";
        this.deliveryFee = Math.max(deliveryFee, 0L);

        for (CartItem item : items) {
            if (safeEquals(item.getFoodId(), food.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                notifyChanged();
                return AddResult.UPDATED;
            }
        }

        CartItem newItem = new CartItem(
                food.getId(),
                food.getTitle(),
                food.getPrice(),
                food.getImageUrl(),
                storeId,
                this.storeName,
                this.deliveryFee,
                quantity
        );
        items.add(newItem);
        notifyChanged();
        return AddResult.ADDED;
    }

    public synchronized void increase(String foodId) {
        for (CartItem item : items) {
            if (safeEquals(item.getFoodId(), foodId)) {
                item.setQuantity(item.getQuantity() + 1);
                notifyChanged();
                return;
            }
        }
    }

    public synchronized void decrease(String foodId) {
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (safeEquals(item.getFoodId(), foodId)) {
                int q = item.getQuantity() - 1;
                if (q <= 0) {
                    items.remove(i);
                } else {
                    item.setQuantity(q);
                }
                resetStoreIfEmpty();
                notifyChanged();
                return;
            }
        }
    }

    public synchronized void remove(String foodId) {
        for (int i = 0; i < items.size(); i++) {
            if (safeEquals(items.get(i).getFoodId(), foodId)) {
                items.remove(i);
                break;
            }
        }
        resetStoreIfEmpty();
        notifyChanged();
    }

    public synchronized void clear() {
        items.clear();
        resetStoreIfEmpty();
        notifyChanged();
    }

    public synchronized List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public synchronized int getItemCount() {
        int count = 0;
        for (CartItem item : items) count += item.getQuantity();
        return count;
    }

    public synchronized long getSubtotal() {
        long sum = 0L;
        for (CartItem item : items) sum += item.getSubtotal();
        return sum;
    }

    public synchronized long getDeliveryFee() {
        return deliveryFee;
    }

    public synchronized long getTotal() {
        return getSubtotal() + getDeliveryFee();
    }

    public synchronized String getStoreId() { return storeId; }
    public synchronized String getStoreName() { return storeName; }
    public synchronized boolean isEmpty() { return items.isEmpty(); }

    public synchronized void registerListener(OnCartChangedListener listener) {
        if (listener == null) return;
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public synchronized void unregisterListener(OnCartChangedListener listener) {
        listeners.remove(listener);
    }

    private void resetStoreIfEmpty() {
        if (!items.isEmpty()) return;
        storeId = "";
        storeName = "";
        deliveryFee = 0L;
    }

    private void notifyChanged() {
        List<OnCartChangedListener> snapshot = new ArrayList<>(listeners);
        for (OnCartChangedListener listener : snapshot) {
            listener.onCartChanged();
        }
    }

    private boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
