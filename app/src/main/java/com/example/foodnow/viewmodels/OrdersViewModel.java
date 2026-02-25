package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrdersViewModel extends ViewModel {

    private final MutableLiveData<List<Order>> ordersLiveData = new MutableLiveData<>();

    public OrdersViewModel() {
        loadOrders();
    }

    public LiveData<List<Order>> getOrders() { return ordersLiveData; }

    private void loadOrders() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        FirebaseFirestore.getInstance()
                .collection("Orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    List<Order> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Order order = doc.toObject(Order.class);
                        order.setId(doc.getId());
                        list.add(order);
                    }
                    ordersLiveData.setValue(list);
                });
    }
}
