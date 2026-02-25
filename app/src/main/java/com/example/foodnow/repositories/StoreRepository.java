package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Store;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StoreRepository {

    private final FirebaseFirestore db;

    public StoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /** Lấy tất cả quán ăn từ Firestore */
    public LiveData<List<Store>> getAllStores() {
        MutableLiveData<List<Store>> liveData = new MutableLiveData<>();

        db.collection("Stores")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    List<Store> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Store store = doc.toObject(Store.class);
                        store.setId(doc.getId());
                        list.add(store);
                    }
                    liveData.setValue(list);
                });

        return liveData;
    }
}
