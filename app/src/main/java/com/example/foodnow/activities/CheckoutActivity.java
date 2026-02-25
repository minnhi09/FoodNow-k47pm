package com.example.foodnow.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodnow.R;
import com.example.foodnow.models.CartItem;
import com.example.foodnow.models.Order;
import com.example.foodnow.utils.CartManager;
import com.example.foodnow.viewmodels.CheckoutViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private CheckoutViewModel viewModel;
    private TextInputEditText etAddress, etNote;
    private MaterialButton btnPlaceOrder;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);

        etAddress     = findViewById(R.id.et_address);
        etNote        = findViewById(R.id.et_note);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        progressBar   = findViewById(R.id.progress_bar);

        CartManager cart = CartManager.getInstance();
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Hiển thị thông tin
        ((TextView) findViewById(R.id.tv_checkout_store)).setText("Quán: " + cart.getCurrentStoreName());
        ((TextView) findViewById(R.id.tv_subtotal)).setText(nf.format(cart.getSubtotal()) + "đ");

        // TODO: lấy phí giao hàng từ Store (tạm dùng 15000)
        long deliveryFee = 15000;
        long total = cart.getSubtotal() + deliveryFee;
        ((TextView) findViewById(R.id.tv_delivery_fee)).setText(nf.format(deliveryFee) + "đ");
        ((TextView) findViewById(R.id.tv_total)).setText(nf.format(total) + "đ");

        // Quan sát kết quả đặt hàng
        viewModel.getOrderSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                cart.clearCart();
                Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        viewModel.getLoadingLiveData().observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnPlaceOrder.setEnabled(!loading);
        });

        // Nút đặt hàng
        btnPlaceOrder.setOnClickListener(v -> {
            String address = etAddress.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            String note = etNote.getText().toString().trim();
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

            // Chuyển CartItem → OrderItem
            List<Order.OrderItem> orderItems = new ArrayList<>();
            for (CartItem ci : cart.getItems()) {
                orderItems.add(new Order.OrderItem(
                        ci.getFoodId(), ci.getTitle(), ci.getPrice(),
                        ci.getQuantity(), ci.getImageUrl()));
            }

            Order order = new Order();
            order.setUserId(userId);
            order.setStoreId(cart.getCurrentStoreId());
            order.setStoreName(cart.getCurrentStoreName());
            order.setAddress(address);
            order.setPaymentMethod("Tiền mặt");
            order.setNote(note);
            order.setSubtotal(cart.getSubtotal());
            order.setDeliveryFee(deliveryFee);
            order.setTotal(total);
            order.setStatus("Đang xử lý");
            order.setCreatedAt(Timestamp.now());
            order.setItems(orderItems);

            viewModel.placeOrder(order);
        });
    }
}
