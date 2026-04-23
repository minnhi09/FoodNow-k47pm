package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Store;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    /** Lấy 1 quán theo ID (real-time) */
    public LiveData<Store> getStoreById(String storeId) {
        MutableLiveData<Store> liveData = new MutableLiveData<>();
        if (storeId == null || storeId.isEmpty()) {
            liveData.setValue(null);
            return liveData;
        }
        db.collection("Stores").document(storeId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null) return;
                    Store store = doc.toObject(Store.class);
                    if (store != null) store.setId(doc.getId());
                    liveData.setValue(store);
                });
        return liveData;
    }

    /** Cập nhật thông tin quán (trả về Task để biết khi nào xong) */
    public Task<Void> updateStore(String storeId, Store store) {
        return db.collection("Stores").document(storeId).set(store);
    }
}