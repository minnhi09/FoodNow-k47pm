package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Food;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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
}
