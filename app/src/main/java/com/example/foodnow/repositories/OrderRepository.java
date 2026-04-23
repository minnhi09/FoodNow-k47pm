package com.example.foodnow.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodnow.models.Order;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
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

    public LiveData<List<Order>> getOrdersByUser(String userId) {
        MutableLiveData<List<Order>> liveData = new MutableLiveData<>();

        db.collection("Orders")
                .whereEqualTo("userId", userId)
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
}
