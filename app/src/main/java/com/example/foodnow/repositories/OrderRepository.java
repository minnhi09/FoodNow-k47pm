package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Order;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    private final FirebaseFirestore db;

    public OrderRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public Task<DocumentReference> createOrder(Order order) {
        return db.collection("Orders").add(order);
    }

    public LiveData<List<Order>> getOrdersByStore(String storeId) {
        MutableLiveData<List<Order>> liveData = new MutableLiveData<>();

        db.collection("Orders")
                .whereEqualTo("storeId", storeId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        return;
                    }

                    List<Order> orderList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Order order = doc.toObject(Order.class);
                        order.setId(doc.getId());
                        orderList.add(order);
                    }
                    liveData.setValue(orderList);
                });

        return liveData;
    }

    public LiveData<List<Order>> getOrdersByUser(String userId) {
        MutableLiveData<List<Order>> liveData = new MutableLiveData<>();

        db.collection("Orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        return;
                    }

                    List<Order> orderList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Order order = doc.toObject(Order.class);
                        order.setId(doc.getId());
                        orderList.add(order);
                    }
                    liveData.setValue(orderList);
                });

        return liveData;
    }

    public Task<Void> updateOrderStatus(String orderId, String newStatus) {
        return db.collection("Orders")
                .document(orderId)
                .update("status", newStatus);
    }

    public Task<Void> cancelOrder(String orderId) {
        return updateOrderStatus(orderId, Order.STATUS_CANCELLED);
    }
}
