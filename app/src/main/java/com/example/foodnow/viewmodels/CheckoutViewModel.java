package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Order;
import com.example.foodnow.repositories.OrderRepository;

public class CheckoutViewModel extends ViewModel {

    private final OrderRepository orderRepository;
    private final MutableLiveData<Boolean> orderSuccess;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;

    public CheckoutViewModel() {
        orderRepository = new OrderRepository();
        orderSuccess = new MutableLiveData<>(false);
        errorLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>(false);
    }

    public LiveData<Boolean> getOrderSuccess() {
        return orderSuccess;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public void placeOrder(Order order) {
        loadingLiveData.setValue(true);
        orderRepository.createOrder(order)
                .addOnSuccessListener(documentReference -> {
                    loadingLiveData.setValue(false);
                    orderSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }
}
