package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Food;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FoodRepository {

    private final FirebaseFirestore db;

    public FoodRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /** Lấy danh sách món theo storeId */
    public LiveData<List<Food>> getFoodsByStore(String storeId) {
        MutableLiveData<List<Food>> liveData = new MutableLiveData<>();

        db.collection("Foods")
                .whereEqualTo("storeId", storeId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    List<Food> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Food food = doc.toObject(Food.class);
                        food.setId(doc.getId());
                        list.add(food);
                    }
                    liveData.setValue(list);
                });

        return liveData;
    }
}
