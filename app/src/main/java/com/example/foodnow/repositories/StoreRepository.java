package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Store;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StoreRepository {

    // ① Kết nối Firestore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ② Method trả về LiveData danh sách stores
    public LiveData<List<Store>> getAllStores() {

        MutableLiveData<List<Store>> liveData = new MutableLiveData<>();

        // ③ Lắng nghe real-time từ collection "Stores"
        db.collection("Stores")
                .addSnapshotListener((snapshots, error) -> {

                    // ④ Nếu có lỗi hoặc data rỗng thì bỏ qua
                    if (error != null || snapshots == null) return;

                    // ⑤ Loop qua từng document, chuyển thành object Store
                    List<Store> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Store store = doc.toObject(Store.class);
                        if (store != null) {
                            store.setId(doc.getId()); // gán id document
                            list.add(store);
                        }
                    }

                    // ⑥ Cập nhật LiveData
                    liveData.setValue(list);
                });

        return liveData;
    }
}