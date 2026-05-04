package com.example.foodnow.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.adapters.CheckoutItemAdapter;
import com.example.foodnow.models.CartItem;
import com.example.foodnow.models.Order;
import com.example.foodnow.models.OrderItem;
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

    private TextView tvCheckoutAddress, tvCheckoutItemsSummary, tvItemsTotalPrice;
    private TextView tvCheckoutItemsCountLabel, tvSubtotal, tvDeliveryFee, tvTotal, tvTotalButton;
    private RecyclerView rvCheckoutItems;
    private View btnBack, btnPlaceOrder;
    private ProgressBar progressBar;

    private CheckoutViewModel viewModel;
    private NumberFormat numberFormat;
    private CartManager cartManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        bindViews();
        
        cartManager = CartManager.getInstance();
        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
        numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        if (cartManager.getItems().isEmpty()) {
            finish();
            return;
        }

        setupUI();
        setupRecyclerView();
        observeViewModel();

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        btnBack.setOnClickListener(v -> finish());
    }

    private void bindViews() {
        tvCheckoutAddress = findViewById(R.id.tv_checkout_address);
        tvCheckoutItemsSummary = findViewById(R.id.tv_checkout_items_summary);
        tvItemsTotalPrice = findViewById(R.id.tv_items_total_price);
        tvCheckoutItemsCountLabel = findViewById(R.id.tv_checkout_items_count_label);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvTotal = findViewById(R.id.tv_total);
        tvTotalButton = findViewById(R.id.tv_total_button);
        rvCheckoutItems = findViewById(R.id.rv_checkout_items);
        btnBack = findViewById(R.id.btn_back);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupUI() {
        long subtotal = cartManager.getSubtotal();
        long total = subtotal + DEFAULT_DELIVERY_FEE;
        int itemCount = cartManager.getItemCount();

        tvCheckoutItemsSummary.setText(itemCount + " món từ " + cartManager.getCurrentStoreName());
        tvItemsTotalPrice.setText(numberFormat.format(subtotal) + "đ");
        tvCheckoutItemsCountLabel.setText("Tạm tính (" + itemCount + " món)");
        
        tvSubtotal.setText(numberFormat.format(subtotal) + "đ");
        tvDeliveryFee.setText(numberFormat.format(DEFAULT_DELIVERY_FEE) + "đ");
        tvTotal.setText(numberFormat.format(total) + "đ");
        tvTotalButton.setText(numberFormat.format(total) + "đ");
    }

    private void setupRecyclerView() {
        CheckoutItemAdapter adapter = new CheckoutItemAdapter(this, cartManager.getItems());
        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        rvCheckoutItems.setAdapter(adapter);
    }

    private void observeViewModel() {
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
                cartManager.clearCart();
                finish();
            }
        });
    }

    private void placeOrder() {
        String address = tvCheckoutAddress.getText().toString().trim();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartManager.getItems()) {
            orderItems.add(new OrderItem(
                    cartItem.getFoodId(),
                    cartItem.getTitle(),
                    cartItem.getPrice(),
                    cartItem.getQuantity(),
                    cartItem.getImageUrl()
            ));
        }

        long subtotal = cartManager.getSubtotal();
        Order order = new Order();
        order.setUserId(currentUser.getUid());
        order.setStoreId(cartManager.getCurrentStoreId());
        order.setStoreName(cartManager.getCurrentStoreName());
        order.setAddress(address);
        order.setPaymentMethod("Tiền mặt");
        order.setNote(""); // Có thể bổ sung Edittext note sau
        order.setSubtotal(subtotal);
        order.setDeliveryFee(DEFAULT_DELIVERY_FEE);
        order.setTotal(subtotal + DEFAULT_DELIVERY_FEE);
        order.setStatus("Đang xử lý");
        order.setCreatedAt(Timestamp.now());
        order.setItems(orderItems);

        viewModel.placeOrder(order);
    }
}
