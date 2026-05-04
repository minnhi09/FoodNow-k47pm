package com.example.foodnow.fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.activities.StoreOwnerActivity;
import com.example.foodnow.adapters.StoreOrderAdapter;
import com.example.foodnow.models.Order;
import com.example.foodnow.models.OrderItem;
import com.example.foodnow.viewmodels.StoreOwnerViewModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Tab "Đơn hàng" trong StoreOwnerActivity.
 * Hiển thị đơn hàng real-time, lọc theo tab trạng thái, cho phép cập nhật status.
 */
public class StoreOrdersFragment extends Fragment {

    // Filter hiện tại: null = tất cả
    private String currentFilter = null;

    private StoreOwnerViewModel viewModel;
    private StoreOrderAdapter   adapter;
    private List<Order>         allOrders = new ArrayList<>();
    private long lastNewCount = -1;

    private final NumberFormat    currFmt = NumberFormat.getInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat timeFmt =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

    private TextView    tabAll, tabNew, tabProcessing, tabDone;
    private TextView    tvCountToday, tvNewBadge, tvTabNewBadge;
    private RecyclerView recyclerView;
    private LinearLayout emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_store_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String storeId = ((StoreOwnerActivity) requireActivity()).getStoreId();
        viewModel = new ViewModelProvider(requireActivity()).get(StoreOwnerViewModel.class);

        recyclerView  = view.findViewById(R.id.rv_store_orders);
        emptyState    = view.findViewById(R.id.empty_orders);
        tvCountToday  = view.findViewById(R.id.tv_order_count_today);
        tvNewBadge    = view.findViewById(R.id.tv_new_order_badge);
        tvTabNewBadge = view.findViewById(R.id.tv_tab_new_badge);
        tabAll        = view.findViewById(R.id.tab_all);
        tabNew        = view.findViewById(R.id.tab_new);
        tabProcessing = view.findViewById(R.id.tab_processing);
        tabDone       = view.findViewById(R.id.tab_done);

        // Setup RecyclerView
        adapter = new StoreOrderAdapter(new StoreOrderAdapter.OnOrderActionListener() {
            @Override
            public void onPrimaryAction(Order order) {
                String nextStatus = nextStatus(order.getStatus());
                if (nextStatus != null) viewModel.updateOrderStatus(order.getId(), nextStatus);
            }

            @Override
            public void onRejectAction(Order order) {
                viewModel.updateOrderStatus(order.getId(), Order.STATUS_CANCELLED);
            }

            @Override
            public void onOrderClick(Order order) {
                showOrderDetailDialog(order);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Filter tab clicks
        tabAll.setOnClickListener(v        -> setFilter(null));
        tabNew.setOnClickListener(v        -> setFilter(Order.STATUS_NEW));
        tabProcessing.setOnClickListener(v -> setFilter(Order.STATUS_PROCESSING));
        tabDone.setOnClickListener(v       -> setFilter(Order.STATUS_DONE));

        // Observe orders
        viewModel.getOrders(storeId).observe(getViewLifecycleOwner(), orders -> {
            allOrders = orders != null ? orders : new ArrayList<>();
            notifyIfNewOrders();
            updateHeader();
            applyFilter();
        });
    }

    private void updateHeader() {
        List<Order> todayOrders = filterToday(allOrders);
        tvCountToday.setText(todayOrders.size() + " đơn hàng hôm nay");

        long newCount = todayOrders.stream()
                .filter(o -> Order.STATUS_NEW.equals(o.getStatus())).count();
        if (newCount > 0) {
            tvNewBadge.setVisibility(View.VISIBLE);
            tvNewBadge.setText("ⓘ " + newCount + " đơn mới");
            tvTabNewBadge.setVisibility(View.VISIBLE);
            tvTabNewBadge.setText(String.valueOf(newCount));
        } else {
            tvNewBadge.setVisibility(View.GONE);
            tvTabNewBadge.setVisibility(View.GONE);
        }
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateTabUI();
        applyFilter();
    }

    private void applyFilter() {
        List<Order> filtered;
        if (currentFilter == null) {
            filtered = allOrders;
        } else if (Order.STATUS_DONE.equals(currentFilter)) {
            // Tab "Xong" = Hoàn thành + Đã hủy
            filtered = allOrders.stream()
                    .filter(o -> Order.STATUS_DONE.equals(o.getStatus())
                            || Order.STATUS_CANCELLED.equals(o.getStatus()))
                    .collect(Collectors.toList());
        } else {
            filtered = allOrders.stream()
                    .filter(o -> currentFilter.equals(o.getStatus()))
                    .collect(Collectors.toList());
        }

        adapter.setOrders(filtered);
        recyclerView.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateTabUI() {
        setTabStyle(tabAll,        null);
        setTabStyle(tabNew,        Order.STATUS_NEW);
        setTabStyle(tabProcessing, Order.STATUS_PROCESSING);
        setTabStyle(tabDone,       Order.STATUS_DONE);
    }

    private void setTabStyle(TextView tab, String filter) {
        boolean selected = (filter == null && currentFilter == null)
                || (filter != null && filter.equals(currentFilter));
        if (selected) {
            tab.setBackgroundResource(R.drawable.bg_tab_selected);
            tab.setTextColor(0xFFFFFFFF);
        } else {
            tab.setBackgroundResource(R.drawable.bg_tab_unselected);
            tab.setTextColor(0xFFFF6B35);
        }
    }

    /** Chuyển sang trạng thái tiếp theo trong flow đơn hàng */
    private String nextStatus(String current) {
        if (current == null) return null;
        switch (current) {
            case Order.STATUS_NEW:        return Order.STATUS_PROCESSING;
            case Order.STATUS_PROCESSING: return Order.STATUS_READY;
            case Order.STATUS_READY:      return Order.STATUS_DONE;  // bỏ qua DELIVERING
            default:                      return null;
        }
    }

    private List<Order> filterToday(List<Order> orders) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        return orders.stream()
                .filter(o -> o.getCreatedAt() != null
                        && o.getCreatedAt().toDate().after(today.getTime()))
                .collect(Collectors.toList());
    }

    private void showOrderDetailDialog(Order order) {
        if (order == null || !isAdded()) return;

        String orderId = order.getId() != null
                ? order.getId().substring(0, Math.min(8, order.getId().length())).toUpperCase()
                : "—";

        StringBuilder sb = new StringBuilder();

        // Trạng thái
        sb.append("Trạng thái: ").append(order.getStatus() != null ? order.getStatus() : "—").append("\n\n");

        // Danh sách món
        sb.append("🛒 Các món đặt:\n");
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                long sub = (long) (item.getPrice() * item.getQuantity());
                sb.append("  • ").append(item.getTitle())
                        .append(" x").append(item.getQuantity())
                        .append("  —  ").append(currFmt.format(sub)).append("đ\n");
            }
        } else {
            sb.append("  (Không có thông tin món)\n");
        }

        // Tiền
        sb.append("\n─────────────────────\n");
        sb.append("Tạm tính:   ").append(currFmt.format((long) order.getSubtotal())).append("đ\n");
        sb.append("Phí giao:   ").append(currFmt.format((long) order.getDeliveryFee())).append("đ\n");
        sb.append("Tổng cộng: ").append(currFmt.format((long) order.getTotal())).append("đ\n\n");

        // Giao hàng
        sb.append("📍 Địa chỉ: ")
                .append(order.getAddress() != null ? order.getAddress() : "—").append("\n");
        sb.append("💳 Thanh toán: ")
                .append(order.getPaymentMethod() != null ? order.getPaymentMethod() : "—").append("\n");

        if (order.getNote() != null && !order.getNote().isEmpty()) {
            sb.append("📝 Ghi chú: ").append(order.getNote()).append("\n");
        }

        // Khách hàng
        if (order.getCustomerName() != null && !order.getCustomerName().isEmpty()) {
            sb.append("👤 Khách: ").append(order.getCustomerName()).append("\n");
        }

        // Thời gian
        if (order.getCreatedAt() != null) {
            sb.append("\n⏱ ").append(timeFmt.format(order.getCreatedAt().toDate()));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Đơn #" + orderId)
                .setMessage(sb.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void notifyIfNewOrders() {
        long newCount = allOrders.stream()
                .filter(o -> Order.STATUS_NEW.equals(o.getStatus()))
                .count();
        if (lastNewCount >= 0 && newCount > lastNewCount) {
            long delta = newCount - lastNewCount;
            String message = "Bạn có " + delta + " đơn mới";
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
            showLocalNotification(message);
        }
        lastNewCount = newCount;
    }

    private void showLocalNotification(String message) {
        String channelId = "owner_orders_channel";
        Context context = requireContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Đơn hàng chủ quán",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo khi có đơn mới");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("FoodNow - Đơn hàng mới")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(1001, builder.build());
    }
}

