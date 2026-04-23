package com.example.foodnow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.adapters.CustomerOrderAdapter;
import com.example.foodnow.models.Order;
import com.example.foodnow.models.OrderItem;
import com.example.foodnow.repositories.OrderRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class OrdersFragment extends Fragment {
    private static final int FILTER_ALL    = 0;
    private static final int FILTER_ACTIVE = 1;
    private static final int FILTER_DONE   = 2;

    private int currentFilter = FILTER_ALL;
    private List<Order> allOrders = new ArrayList<>();

    private CustomerOrderAdapter adapter;
    private OrderRepository orderRepository;
    private TextView tvCount;
    private TextView tabAll, tabActive, tabDone;
    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;

    private final NumberFormat currFmt = NumberFormat.getInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat timeFmt =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCount     = view.findViewById(R.id.tv_customer_order_count);
        tabAll      = view.findViewById(R.id.tab_customer_all);
        tabActive   = view.findViewById(R.id.tab_customer_active);
        tabDone     = view.findViewById(R.id.tab_customer_done);
        rvOrders    = view.findViewById(R.id.rv_customer_orders);
        layoutEmpty = view.findViewById(R.id.layout_customer_orders_empty);

        orderRepository = new OrderRepository();

        adapter = new CustomerOrderAdapter();
        adapter.setListener(new CustomerOrderAdapter.OnOrderActionListener() {
            @Override
            public void onOrderClick(Order order) {
                showOrderDetailDialog(order);
            }

            @Override
            public void onOrderCancel(Order order) {
                showCancelConfirmDialog(order);
            }
        });
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(adapter);

        tabAll.setOnClickListener(v -> setFilter(FILTER_ALL));
        tabActive.setOnClickListener(v -> setFilter(FILTER_ACTIVE));
        tabDone.setOnClickListener(v -> setFilter(FILTER_DONE));
        updateTabUI();

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";
        if (uid.isEmpty()) {
            allOrders = new ArrayList<>();
            applyFilter();
            return;
        }

        orderRepository.getOrdersByUser(uid).observe(getViewLifecycleOwner(), orders -> {
            allOrders = orders != null ? orders : new ArrayList<>();
            applyFilter();
        });
    }

    private void setFilter(int filter) {
        currentFilter = filter;
        updateTabUI();
        applyFilter();
    }

    private void applyFilter() {
        List<Order> filtered;
        if (currentFilter == FILTER_ALL) {
            filtered = allOrders;
        } else if (currentFilter == FILTER_ACTIVE) {
            filtered = allOrders.stream()
                    .filter(o -> !Order.STATUS_DONE.equals(o.getStatus())
                            && !Order.STATUS_CANCELLED.equals(o.getStatus()))
                    .collect(Collectors.toList());
        } else {
            filtered = allOrders.stream()
                    .filter(o -> Order.STATUS_DONE.equals(o.getStatus())
                            || Order.STATUS_CANCELLED.equals(o.getStatus()))
                    .collect(Collectors.toList());
        }

        tvCount.setText(filtered.size() + " đơn");
        adapter.setOrders(filtered);
        rvOrders.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateTabUI() {
        setTabStyle(tabAll,    currentFilter == FILTER_ALL);
        setTabStyle(tabActive, currentFilter == FILTER_ACTIVE);
        setTabStyle(tabDone,   currentFilter == FILTER_DONE);
    }

    private void setTabStyle(TextView tab, boolean selected) {
        if (selected) {
            tab.setBackgroundResource(R.drawable.bg_tab_selected);
            tab.setTextColor(0xFFFFFFFF);
        } else {
            tab.setBackgroundResource(R.drawable.bg_tab_unselected);
            tab.setTextColor(0xFFD46E1F);
        }
    }

    // ── Chi tiết đơn hàng ──────────────────────────────────

    private void showOrderDetailDialog(Order order) {
        if (order == null || !isAdded()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("📍 Địa chỉ: ").append(safeStr(order.getAddress())).append("\n");
        sb.append("💳 Thanh toán: ").append(safeStr(order.getPaymentMethod())).append("\n");

        if (order.getNote() != null && !order.getNote().isEmpty()) {
            sb.append("📝 Ghi chú: ").append(order.getNote()).append("\n");
        }

        sb.append("\n🛒 Các món đã đặt:\n");
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                long subtotal = (long) (item.getPrice() * item.getQuantity());
                sb.append("  • ").append(item.getTitle())
                        .append(" x").append(item.getQuantity())
                        .append("  —  ").append(currFmt.format(subtotal)).append("đ\n");
            }
        }

        sb.append("\n─────────────────────\n");
        sb.append("Tạm tính: ").append(currFmt.format((long) order.getSubtotal())).append("đ\n");
        sb.append("Phí giao: ").append(currFmt.format((long) order.getDeliveryFee())).append("đ\n");
        sb.append("Tổng cộng: ").append(currFmt.format((long) order.getTotal())).append("đ");

        if (order.getCreatedAt() != null) {
            sb.append("\n\n⏱ ").append(timeFmt.format(order.getCreatedAt().toDate()));
        }

        String orderId = order.getId() != null
                ? order.getId().substring(0, Math.min(8, order.getId().length())).toUpperCase()
                : "—";

        new AlertDialog.Builder(requireContext())
                .setTitle("Đơn #" + orderId)
                .setMessage(sb.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }

    // ── Hủy đơn hàng ───────────────────────────────────────

    private void showCancelConfirmDialog(Order order) {
        if (order == null || order.getId() == null || !isAdded()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Hủy đơn hàng?")
                .setMessage("Bạn chắc chắn muốn hủy đơn hàng này?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    orderRepository.cancelOrder(order.getId())
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(requireContext(),
                                            "Đã hủy đơn hàng",
                                            Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(),
                                            "Hủy đơn thất bại: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private String safeStr(String s) {
        return s != null ? s : "—";
    }
}

