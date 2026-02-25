package com.example.foodnow.repositories;

import com.example.foodnow.models.Order;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrderRepository {

    private final FirebaseFirestore db;

    public OrderRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /** Tạo đơn hàng mới trên Firestore */
    public Task<DocumentReference> createOrder(Order order) {
        return db.collection("Orders").add(order);
    }
}
