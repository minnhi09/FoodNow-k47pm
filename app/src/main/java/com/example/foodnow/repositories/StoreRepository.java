package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Store;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** Cập nhật chỉ field imageUrl (partial update, không ghi đè toàn document) */
    public Task<Void> updateStoreImageUrl(String storeId, String imageUrl) {
        return db.collection("Stores").document(storeId).update("imageUrl", imageUrl);
    }

    /** Cập nhật nhiều field cùng lúc (partial update) */
    public Task<Void> updateStoreFields(String storeId, Map<String, Object> fields) {
        return db.collection("Stores").document(storeId).update(fields);
    }

    /**
     * Batch-fetch tên quán cho danh sách storeId.
     * Trả về Task<Map<storeId, storeName>> — one-shot, không realtime.
     */
    public Task<Map<String, String>> getStoreNamesByIds(List<String> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Tasks.forResult(new HashMap<>());
        }
        // Loại bỏ trùng lặp
        List<String> uniqueIds = new ArrayList<>();
        for (String id : storeIds) {
            if (id != null && !id.isEmpty() && !uniqueIds.contains(id)) {
                uniqueIds.add(id);
            }
        }
        // Tạo danh sách Task<DocumentSnapshot> cho mỗi storeId
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : uniqueIds) {
            tasks.add(db.collection("Stores").document(id).get());
        }
        // Khi tất cả task hoàn thành, ghép kết quả thành map
        return Tasks.whenAll(tasks).continueWith(task -> {
            Map<String, String> result = new HashMap<>();
            for (int i = 0; i < tasks.size(); i++) {
                DocumentSnapshot doc = tasks.get(i).getResult();
                if (doc != null && doc.exists()) {
                    String name = doc.getString("name");
                    if (name != null) {
                        result.put(uniqueIds.get(i), name);
                    }
                }
            }
            return result;
        });
    }
}