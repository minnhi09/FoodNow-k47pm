package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Food;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodRepository {

    // ① Kết nối Firestore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ② Lấy danh sách món ăn theo quán
    public LiveData<List<Food>> getFoodsByStore(String storeId) {

        MutableLiveData<List<Food>> liveData = new MutableLiveData<>();

        // ③ Lọc Foods theo storeId, lắng nghe real-time
        db.collection("Foods")
                .whereEqualTo("storeId", storeId)
                .addSnapshotListener((snapshots, error) -> {

                    // ④ Bỏ qua nếu có lỗi hoặc data rỗng
                    if (error != null || snapshots == null) return;

                    // ⑤ Chuyển từng document thành object Food
                    List<Food> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Food food = doc.toObject(Food.class);
                        if (food != null) {
                            food.setId(doc.getId()); // gán id document
                            list.add(food);
                        }
                    }

                    // ⑥ Cập nhật LiveData
                    liveData.setValue(list);
                });

        return liveData;
    }

    /** Top 6 món có rating cao nhất (cross-store), không cần composite Firestore index */
    public LiveData<List<Food>> getTopRatedFoods() {
        MutableLiveData<List<Food>> liveData = new MutableLiveData<>();
        db.collection("Foods")
                .orderBy("rating", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(8)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    List<Food> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Food food = doc.toObject(Food.class);
                        if (food != null && food.isAvailable()) {
                            food.setId(doc.getId());
                            list.add(food);
                            if (list.size() == 6) break; // tối đa 6 món sau khi lọc
                        }
                    }
                    liveData.setValue(list);
                });
        return liveData;
    }

    /** Thêm món ăn mới — Firestore tự tạo ID */
    public Task<Void> addFood(Food food) {
        // Dùng Map để tránh lưu field "id" (id là doc key, không phải field)
        Map<String, Object> data = foodToMap(food);
        return db.collection("Foods").document().set(data);
    }

    /** Cập nhật món ăn đã có */
    public Task<Void> updateFood(Food food) {
        Map<String, Object> data = foodToMap(food);
        return db.collection("Foods").document(food.getId()).set(data);
    }

    /** Xóa món ăn */
    public Task<Void> deleteFood(String foodId) {
        return db.collection("Foods").document(foodId).delete();
    }

    /** Cập nhật nhanh trạng thái isAvailable của món ăn */
    public Task<Void> updateFoodAvailability(String foodId, boolean isAvailable) {
        return db.collection("Foods").document(foodId).update("isAvailable", isAvailable);
    }

    private Map<String, Object> foodToMap(Food food) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", food.getTitle());
        map.put("description", food.getDescription());
        map.put("price", food.getPrice());
        map.put("imageUrl", food.getImageUrl());
        map.put("rating", food.getRating());
        map.put("storeId", food.getStoreId());
        map.put("categoryId", food.getCategoryId());
        map.put("isAvailable", food.isAvailable());
        return map;
    }
}
