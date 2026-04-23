package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Order;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/** Truy cập Firestore collection "Orders" cho phía chủ quán. */
public class OrderRepository {

    private final FirebaseFirestore db;

    public OrderRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Lắng nghe real-time danh sách đơn hàng của một quán.
     * Sắp xếp theo createdAt giảm dần (mới nhất trước).
     */
    public LiveData<List<Order>> getOrdersByStore(String storeId) {
        MutableLiveData<List<Order>> liveData = new MutableLiveData<>();
        if (storeId == null || storeId.isEmpty()) {
            liveData.setValue(new ArrayList<>());
            return liveData;
        }
        db.collection("Orders")
                .whereEqualTo("storeId", storeId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Order order = doc.toObject(Order.class);
                        order.setId(doc.getId());
                        orders.add(order);
                    }
                    liveData.setValue(orders);
                });

        return liveData;
    }

    /**
     * Cập nhật trạng thái đơn hàng.
     * Trả về Task để caller có thể xử lý success/failure.
     */
    public Task<Void> updateOrderStatus(String orderId, String newStatus) {
        return db.collection("Orders")
                .document(orderId)
                .update("status", newStatus);
    }

    /** Tạo đơn hàng mới từ phía customer. */
    public Task<Void> createOrder(Order order) {
        if (order == null) {
            return Tasks.forException(new IllegalArgumentException("Order không hợp lệ"));
        }
        return db.collection("Orders")
                .add(order)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException() != null
                                ? task.getException()
                                : new Exception("Tạo đơn hàng thất bại");
                    }
                    return Tasks.forResult(null);
                });
    }

    /** Customer hủy đơn khi trạng thái còn là "Đơn mới". */
    public Task<Void> cancelOrder(String orderId) {
        return updateOrderStatus(orderId, Order.STATUS_CANCELLED);
    }

    /**
     * Lắng nghe real-time đơn hàng của một user (dùng cho phía khách hàng).
     */
    public LiveData<List<Order>> getOrdersByUser(String userId) {
        MutableLiveData<List<Order>> liveData = new MutableLiveData<>();

        db.collection("Orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Order order = doc.toObject(Order.class);
                        order.setId(doc.getId());
                        orders.add(order);
                    }
                    liveData.setValue(orders);
                });

        return liveData;
    }
}
