package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Order;
import com.example.foodnow.repositories.OrderRepository;

import java.util.ArrayList;
import java.util.List;

public class OrdersViewModel extends ViewModel {

    private final OrderRepository orderRepository;
    private final MediatorLiveData<List<Order>> ordersLiveData = new MediatorLiveData<>();
    private LiveData<List<Order>> currentSource;

    public OrdersViewModel() {
        orderRepository = new OrderRepository();
        ordersLiveData.setValue(new ArrayList<>());
    }

    public LiveData<List<Order>> getOrdersLiveData() {
        return ordersLiveData;
    }

    public void fetchOrders(String userId) {
        if (currentSource != null) {
            ordersLiveData.removeSource(currentSource);
        }

        if (userId == null || userId.trim().isEmpty()) {
            ordersLiveData.setValue(new ArrayList<>());
            return;
        }

        currentSource = orderRepository.getOrdersByUser(userId);
        ordersLiveData.addSource(currentSource, orders -> {
            ordersLiveData.setValue(orders);
        });
    }
}
