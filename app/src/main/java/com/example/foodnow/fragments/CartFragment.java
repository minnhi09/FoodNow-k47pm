package com.example.foodnow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.adapters.CartItemAdapter;
import com.example.foodnow.models.CartItem;
import com.example.foodnow.models.Order;
import com.example.foodnow.models.OrderItem;
import com.example.foodnow.models.User;
import com.example.foodnow.repositories.OrderRepository;
import com.example.foodnow.repositories.UserRepository;
import com.example.foodnow.utils.CartManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Giỏ hàng customer: local cart + checkout tạo Orders trên Firestore.
 */
public class CartFragment extends Fragment {
    private CartManager cartManager;
    private CartItemAdapter adapter;
    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private NumberFormat currFmt;

    private LinearLayout layoutEmpty;
    private LinearLayout layoutContent;
    private TextView tvStoreName;
    private TextView tvAddress;
    private TextView tvSubtotal;
    private TextView tvDeliveryFee;
    private TextView tvTotal;
    private EditText etNote;
    private RadioGroup rgPayment;
    private MaterialButton btnPlaceOrder;

    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartManager = CartManager.getInstance();
        userRepository = new UserRepository();
        orderRepository = new OrderRepository();
        currFmt = NumberFormat.getInstance(new Locale("vi", "VN"));

        layoutEmpty = view.findViewById(R.id.layout_cart_empty);
        layoutContent = view.findViewById(R.id.layout_cart_content);
        tvStoreName = view.findViewById(R.id.tv_cart_store_name);
        tvAddress = view.findViewById(R.id.tv_cart_address);
        tvSubtotal = view.findViewById(R.id.tv_cart_subtotal);
        tvDeliveryFee = view.findViewById(R.id.tv_cart_delivery_fee);
        tvTotal = view.findViewById(R.id.tv_cart_total);
        etNote = view.findViewById(R.id.et_cart_note);
        rgPayment = view.findViewById(R.id.rg_payment_method);
        btnPlaceOrder = view.findViewById(R.id.btn_place_order);
        RecyclerView rvItems = view.findViewById(R.id.rv_cart_items);

        adapter = new CartItemAdapter(new CartItemAdapter.OnCartItemActionListener() {
            @Override
            public void onIncrease(CartItem item) {
                cartManager.increase(item.getFoodId());
                renderCart();
            }

            @Override
            public void onDecrease(CartItem item) {
                cartManager.decrease(item.getFoodId());
                renderCart();
            }

            @Override
            public void onRemove(CartItem item) {
                cartManager.remove(item.getFoodId());
                renderCart();
            }
        });
        rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvItems.setAdapter(adapter);

        userRepository.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            currentUser = user;
            if (user == null || user.getAddress() == null || user.getAddress().trim().isEmpty()) {
                tvAddress.setText("Địa chỉ: Chưa thiết lập địa chỉ");
            } else {
                tvAddress.setText("Địa chỉ: " + user.getAddress());
            }
        });

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        renderCart();
    }

    private void renderCart() {
        List<CartItem> items = cartManager.getItems();
        boolean isEmpty = items.isEmpty();

        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        layoutContent.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        adapter.setItems(items);

        String storeName = cartManager.getStoreName();
        tvStoreName.setText(storeName == null || storeName.isEmpty() ? "Chưa chọn quán" : storeName);

        long subtotal = cartManager.getSubtotal();
        long deliveryFee = cartManager.getDeliveryFee();
        long total = cartManager.getTotal();

        tvSubtotal.setText("Tạm tính: " + currFmt.format(subtotal) + "đ");
        tvDeliveryFee.setText("Phí giao hàng: " + currFmt.format(deliveryFee) + "đ");
        tvTotal.setText("Tổng cộng: " + currFmt.format(total) + "đ");
    }

    private void placeOrder() {
        if (cartManager.isEmpty()) {
            Toast.makeText(requireContext(), "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";
        if (uid.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn cần đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String address = currentUser != null ? currentUser.getAddress() : "";
        if (address == null || address.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng cập nhật địa chỉ giao hàng trước", Toast.LENGTH_SHORT).show();
            return;
        }

        Order order = new Order();
        order.setUserId(uid);
        order.setStoreId(cartManager.getStoreId());
        order.setStoreName(cartManager.getStoreName());
        order.setCustomerName(currentUser != null ? currentUser.getName() : "Khách hàng");
        order.setAddress(address);
        order.setPaymentMethod(getPaymentMethod());
        order.setNote(etNote.getText() != null ? etNote.getText().toString().trim() : "");
        order.setSubtotal(cartManager.getSubtotal());
        order.setDeliveryFee(cartManager.getDeliveryFee());
        order.setTotal(cartManager.getTotal());
        order.setStatus(Order.STATUS_NEW);
        order.setCreatedAt(Timestamp.now());
        order.setItems(toOrderItems(cartManager.getItems()));

        btnPlaceOrder.setEnabled(false);
        orderRepository.createOrder(order)
                .addOnSuccessListener(unused -> {
                    cartManager.clear();
                    btnPlaceOrder.setEnabled(true);
                    renderCart();
                    Toast.makeText(requireContext(), "Đặt hàng thành công", Toast.LENGTH_SHORT).show();
                    BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_navigation);
                    if (nav != null) nav.setSelectedItemId(R.id.nav_orders);
                })
                .addOnFailureListener(e -> {
                    btnPlaceOrder.setEnabled(true);
                    Toast.makeText(requireContext(), "Đặt hàng thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String getPaymentMethod() {
        int checkedId = rgPayment.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_payment_transfer) return "Chuyển khoản";
        return "Tiền mặt";
    }

    private List<OrderItem> toOrderItems(List<CartItem> cartItems) {
        List<OrderItem> result = new ArrayList<>();
        for (CartItem item : cartItems) {
            result.add(new OrderItem(
                    item.getFoodId(),
                    item.getTitle(),
                    item.getPrice(),
                    item.getQuantity(),
                    item.getImageUrl()
            ));
        }
        return result;
    }
}
