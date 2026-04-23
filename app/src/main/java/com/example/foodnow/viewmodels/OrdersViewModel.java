package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Order;
import com.example.foodnow.repositories.OrderRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class OrdersViewModel extends ViewModel {

    private final OrderRepository orderRepository;
    private final LiveData<List<Order>> orders;

    public OrdersViewModel() {
        orderRepository = new OrderRepository();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        if (userId != null && !userId.trim().isEmpty()) {
            orders = orderRepository.getOrdersByUser(userId);
        } else {
            MutableLiveData<List<Order>> emptyLiveData = new MutableLiveData<>();
            emptyLiveData.setValue(new ArrayList<>());
            orders = emptyLiveData;
        }
    }

    public LiveData<List<Order>> getOrders() {
        return orders;
    }
}
