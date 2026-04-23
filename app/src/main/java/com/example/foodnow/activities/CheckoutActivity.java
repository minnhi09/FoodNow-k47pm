package com.example.foodnow.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodnow.R;
import com.example.foodnow.models.CartItem;
import com.example.foodnow.models.Order;
import com.example.foodnow.utils.CartManager;
import com.example.foodnow.viewmodels.CheckoutViewModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private static final long DEFAULT_DELIVERY_FEE = 15000L;

    private TextView tvCheckoutStore;
    private TextView tvSubtotal;
    private TextView tvDeliveryFee;
    private TextView tvTotal;
    private EditText etAddress;
    private EditText etNote;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;

    private CheckoutViewModel viewModel;
    private NumberFormat numberFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        tvCheckoutStore = findViewById(R.id.tv_checkout_store);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvTotal = findViewById(R.id.tv_total);
        etAddress = findViewById(R.id.et_address);
        etNote = findViewById(R.id.et_note);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        progressBar = findViewById(R.id.progress_bar);

        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
        numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        if (CartManager.getInstance().getItems().isEmpty()) {
            finish();
            return;
        }

        tvCheckoutStore.setText(CartManager.getInstance().getCurrentStoreName());

        long subtotal = CartManager.getInstance().getSubtotal();
        long deliveryFee = DEFAULT_DELIVERY_FEE;
        long total = subtotal + deliveryFee;

        tvSubtotal.setText("Tạm tính: " + numberFormat.format(subtotal) + "đ");
        tvDeliveryFee.setText("Phí giao hàng: " + numberFormat.format(deliveryFee) + "đ");
        tvTotal.setText("Tổng thanh toán: " + numberFormat.format(total) + "đ");

        viewModel.getLoadingLiveData().observe(this, isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnPlaceOrder.setEnabled(!loading);
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.trim().isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getOrderSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();
                CartManager.getInstance().clearCart();
                finish();
            }
        });

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
        String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";

        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;
        if (userId == null || userId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Order.OrderItem> orderItems = new ArrayList<>();
        List<CartItem> cartItems = CartManager.getInstance().getItems();
        for (CartItem cartItem : cartItems) {
            Order.OrderItem orderItem = new Order.OrderItem(
                    cartItem.getFoodId(),
                    cartItem.getTitle(),
                    cartItem.getPrice(),
                    cartItem.getQuantity(),
                    cartItem.getImageUrl()
            );
            orderItems.add(orderItem);
        }

        long subtotal = CartManager.getInstance().getSubtotal();
        long deliveryFee = DEFAULT_DELIVERY_FEE;
        long total = subtotal + deliveryFee;

        Order order = new Order();
        order.setUserId(userId);
        order.setStoreId(CartManager.getInstance().getCurrentStoreId());
        order.setStoreName(CartManager.getInstance().getCurrentStoreName());
        order.setAddress(address);
        order.setPaymentMethod("Tiền mặt");
        order.setNote(note);
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotal(total);
        order.setStatus("Đang xử lý");
        order.setCreatedAt(Timestamp.now());
        order.setItems(orderItems);

        viewModel.placeOrder(order);
    }
}
