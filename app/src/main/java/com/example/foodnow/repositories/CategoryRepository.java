package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Category;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    private final FirebaseFirestore db;

    public CategoryRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /** Lấy tất cả danh mục từ Firestore */
    public LiveData<List<Category>> getAllCategories() {
        MutableLiveData<List<Category>> liveData = new MutableLiveData<>();

        db.collection("Categories")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    List<Category> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Category category = doc.toObject(Category.class);
                        category.setId(doc.getId());
                        list.add(category);
                    }
                    liveData.setValue(list);
                });

        return liveData;
    }
}
