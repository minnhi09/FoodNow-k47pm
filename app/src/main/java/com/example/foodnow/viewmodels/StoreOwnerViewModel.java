package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Food;
import com.example.foodnow.models.Order;
import com.example.foodnow.models.Store;
import com.example.foodnow.repositories.FoodRepository;
import com.example.foodnow.repositories.OrderRepository;
import com.example.foodnow.repositories.StoreRepository;

import java.util.List;

/**
 * ViewModel dùng riêng cho StoreOwnerActivity và các fragment con.
 * Cung cấp dữ liệu quán + thực đơn, và các thao tác CRUD món ăn.
 */
public class StoreOwnerViewModel extends ViewModel {

    private final StoreRepository storeRepo = new StoreRepository();
    private final FoodRepository  foodRepo  = new FoodRepository();
    private final OrderRepository orderRepo = new OrderRepository();

    private LiveData<Store>       storeLiveData;
    private LiveData<List<Food>>  foodsLiveData;
    private LiveData<List<Order>> ordersLiveData;

    // Thông báo trạng thái thao tác (thêm/sửa/xóa)
    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();
    // Loading indicator
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    // ─── Store ────────────────────────────────────────────

    /** Lấy thông tin quán real-time (lazy init theo storeId) */
    public LiveData<Store> getStore(String storeId) {
        if (storeLiveData == null) {
            storeLiveData = storeRepo.getStoreById(storeId);
        }
        return storeLiveData;
    }

    // ─── Foods ────────────────────────────────────────────

    /** Lấy danh sách món ăn real-time theo quán (lazy init) */
    public LiveData<List<Food>> getFoods(String storeId) {
        if (foodsLiveData == null) {
            foodsLiveData = foodRepo.getFoodsByStore(storeId);
        }
        return foodsLiveData;
    }

    // ─── CRUD món ăn ──────────────────────────────────────

    public void addFood(Food food) {
        loading.setValue(true);
        foodRepo.addFood(food)
                .addOnSuccessListener(v -> {
                    loading.setValue(false);
                    actionMessage.setValue("Thêm món ăn thành công!");
                })
                .addOnFailureListener(e -> {
                    loading.setValue(false);
                    actionMessage.setValue("Lỗi: " + e.getMessage());
                });
    }

    public void updateFood(Food food) {
        loading.setValue(true);
        foodRepo.updateFood(food)
                .addOnSuccessListener(v -> {
                    loading.setValue(false);
                    actionMessage.setValue("Cập nhật món ăn thành công!");
                })
                .addOnFailureListener(e -> {
                    loading.setValue(false);
                    actionMessage.setValue("Lỗi: " + e.getMessage());
                });
    }

    public void deleteFood(String foodId) {
        loading.setValue(true);
        foodRepo.deleteFood(foodId)
                .addOnSuccessListener(v -> {
                    loading.setValue(false);
                    actionMessage.setValue("Đã xóa món ăn.");
                })
                .addOnFailureListener(e -> {
                    loading.setValue(false);
                    actionMessage.setValue("Lỗi xóa: " + e.getMessage());
                });
    }

    // ─── Getters LiveData ─────────────────────────────────

    public LiveData<String> getActionMessage() { return actionMessage; }
    public LiveData<Boolean> getLoading()      { return loading; }

    // ─── Orders ───────────────────────────────────────────

    /** Lấy danh sách đơn hàng real-time theo quán (lazy init) */
    public LiveData<List<Order>> getOrders(String storeId) {
        if (ordersLiveData == null) {
            ordersLiveData = orderRepo.getOrdersByStore(storeId);
        }
        return ordersLiveData;
    }

    /** Cập nhật trạng thái đơn hàng */
    public void updateOrderStatus(String orderId, String newStatus) {
        orderRepo.updateOrderStatus(orderId, newStatus)
                .addOnSuccessListener(v ->
                        actionMessage.setValue("Đã cập nhật trạng thái đơn hàng."))
                .addOnFailureListener(e ->
                        actionMessage.setValue("Lỗi cập nhật đơn: " + e.getMessage()));
    }
}
