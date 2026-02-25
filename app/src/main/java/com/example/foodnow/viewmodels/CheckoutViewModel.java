package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Order;
import com.example.foodnow.repositories.OrderRepository;

public class CheckoutViewModel extends ViewModel {

    private final OrderRepository orderRepository;
    private final MutableLiveData<Boolean> orderSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    public CheckoutViewModel() {
        orderRepository = new OrderRepository();
    }

    public LiveData<Boolean> getOrderSuccess()    { return orderSuccess; }
    public LiveData<String> getErrorLiveData()    { return errorLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }

    /** Đặt hàng */
    public void placeOrder(Order order) {
        loadingLiveData.setValue(true);
        orderRepository.createOrder(order)
                .addOnSuccessListener(ref -> {
                    loadingLiveData.setValue(false);
                    orderSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }
}
