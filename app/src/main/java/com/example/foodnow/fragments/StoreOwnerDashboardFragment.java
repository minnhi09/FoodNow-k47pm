package com.example.foodnow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.activities.StoreOwnerActivity;
import com.example.foodnow.models.Order;
import com.example.foodnow.models.OrderItem;
import com.example.foodnow.repositories.UserRepository;
import com.example.foodnow.viewmodels.StoreOwnerViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Tab "Tổng quan" — dashboard chủ quán.
 * Hiển thị: header cam, toggle mở/đóng, stat cards, đơn hàng mới nhất, món bán chạy.
 */
public class StoreOwnerDashboardFragment extends Fragment {

    private StoreOwnerViewModel viewModel;
    private NumberFormat currFmt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_store_owner_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String storeId = ((StoreOwnerActivity) requireActivity()).getStoreId();
        viewModel = new ViewModelProvider(requireActivity()).get(StoreOwnerViewModel.class);
        currFmt = NumberFormat.getInstance(new Locale("vi", "VN"));

        ImageView imgCover           = view.findViewById(R.id.img_store_cover);
        TextView  tvStoreName        = view.findViewById(R.id.tv_store_name);
        TextView  tvOpenStatus       = view.findViewById(R.id.tv_open_status);
        TextView  tvOpenHint         = view.findViewById(R.id.tv_open_hint);
        Switch    switchOpen         = view.findViewById(R.id.switch_open);
        TextView  tvRevenue          = view.findViewById(R.id.tv_stat_revenue);
        TextView  tvRevenueChange    = view.findViewById(R.id.tv_stat_revenue_change);
        TextView  tvOrders           = view.findViewById(R.id.tv_stat_orders);
        TextView  tvOrdersChange     = view.findViewById(R.id.tv_stat_orders_change);
        TextView  tvRating           = view.findViewById(R.id.tv_stat_rating);
        TextView  tvNewCustomers     = view.findViewById(R.id.tv_stat_new_customers);
        LinearLayout containerOrders = view.findViewById(R.id.container_recent_orders);
        TextView  tvNoOrders         = view.findViewById(R.id.tv_no_recent_orders);
        TextView  tvSeeAll           = view.findViewById(R.id.tv_see_all_orders);
        LinearLayout containerTopFoods = view.findViewById(R.id.container_top_foods);
        TextView  tvNoTopFoods       = view.findViewById(R.id.tv_no_top_foods);

        // Xem tất cả đơn hàng → chuyển sang tab Đơn hàng
        tvSeeAll.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.owner_fragment_container, new StoreOrdersFragment())
                        .commit());

        // ── Dữ liệu quán ──
        viewModel.getStore(storeId).observe(getViewLifecycleOwner(), store -> {
            if (store == null) return;

            tvStoreName.setText(store.getName());
            tvRating.setText(String.format(Locale.US, "%.1f ⭐", store.getRating()));

            switchOpen.setChecked(store.isOpen());
            updateOpenStatusUI(tvOpenStatus, tvOpenHint, store.isOpen());

            switchOpen.setOnCheckedChangeListener((btn, isChecked) -> {
                store.setOpen(isChecked);
                updateOpenStatusUI(tvOpenStatus, tvOpenHint, isChecked);
                new com.example.foodnow.repositories.StoreRepository()
                        .updateStore(storeId, store);
            });
        });

        // Load avatar chủ cửa hàng (user hiện tại) vào ảnh tròn trên header
        new UserRepository().getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            String avatarUrl = user.getImageUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .circleCrop()
                        .into(imgCover);
            }
        });

        // ── Đơn hàng — stat cards + recent list + top foods ──
        viewModel.getOrders(storeId).observe(getViewLifecycleOwner(), orders -> {
            List<Order> todayOrders = filterToday(orders);

            // Stat: số đơn hôm nay
            int orderCount = todayOrders.size();
            tvOrders.setText(String.valueOf(orderCount));
            tvOrdersChange.setText(orderCount > 0 ? "↑ +" + orderCount : "");
            tvOrdersChange.setTextColor(0xFF4CAF50);

            // Stat: doanh thu hôm nay (chỉ đơn Hoàn thành)
            double revenue = 0;
            for (Order o : todayOrders) {
                if (Order.STATUS_DONE.equals(o.getStatus())) {
                    revenue += o.getTotal();
                }
            }
            tvRevenue.setText(currFmt.format((long) revenue) + "đ");
            tvRevenueChange.setText(revenue > 0 ? "↑ +" + currFmt.format((long) revenue / 1000) + "K" : "");
            tvRevenueChange.setTextColor(0xFF4CAF50);

            // Stat: khách mới (unique userId hôm nay)
            long uniqueCustomers = todayOrders.stream()
                    .map(Order::getUserId).distinct().count();
            tvNewCustomers.setText(String.valueOf(uniqueCustomers));

            // Recent orders (tối đa 3 đơn mới nhất)
            containerOrders.removeAllViews();
            List<Order> recent = orders.size() > 3 ? orders.subList(0, 3) : orders;
            if (recent.isEmpty()) {
                tvNoOrders.setVisibility(View.VISIBLE);
            } else {
                tvNoOrders.setVisibility(View.GONE);
                for (Order order : recent) {
                    addRecentOrderCard(containerOrders, order, storeId);
                }
            }

            // Top foods
            bindTopFoods(containerTopFoods, tvNoTopFoods, todayOrders);
        });
    }

    /** Hiển thị 1 card đơn hàng tóm tắt trong dashboard. */
    private void addRecentOrderCard(LinearLayout container, Order order, String storeId) {
        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_owner_order_mini, container, false);

        TextView tvOrderId      = card.findViewById(R.id.tv_mini_order_id);
        TextView tvStatus       = card.findViewById(R.id.tv_mini_status);
        TextView tvTimeAgo      = card.findViewById(R.id.tv_mini_time_ago);
        TextView tvCustomerName = card.findViewById(R.id.tv_mini_customer_name);
        TextView tvItems        = card.findViewById(R.id.tv_mini_items);
        TextView tvTotal        = card.findViewById(R.id.tv_mini_total);
        TextView tvConfirm      = card.findViewById(R.id.btn_mini_confirm);
        TextView tvReject       = card.findViewById(R.id.btn_mini_reject);
        TextView tvActionStatus = card.findViewById(R.id.tv_mini_action_status);

        tvOrderId.setText("#GF" + order.getId().substring(0, Math.min(7, order.getId().length())).toUpperCase());
        tvItems.setText(order.getItemsSummary());
        tvTotal.setText(currFmt.format((long) order.getTotal()) + "đ");

        // Tên khách
        String name = order.getCustomerName();
        tvCustomerName.setText((name != null && !name.isEmpty()) ? name : "Khách hàng");

        // Thời gian tương đối
        if (order.getCreatedAt() != null) {
            tvTimeAgo.setText("⏱ " + timeAgo(order.getCreatedAt().toDate().getTime()));
        }

        // Badge màu theo trạng thái
        applyStatusBadge(tvStatus, order.getStatus());

        // Nút hành động theo trạng thái
        if (Order.STATUS_NEW.equals(order.getStatus())) {
            tvConfirm.setVisibility(View.VISIBLE);
            tvReject.setVisibility(View.VISIBLE);
            tvConfirm.setOnClickListener(v ->
                    viewModel.updateOrderStatus(order.getId(), Order.STATUS_PROCESSING));
            tvReject.setOnClickListener(v ->
                    viewModel.updateOrderStatus(order.getId(), Order.STATUS_CANCELLED));
        } else if (Order.STATUS_PROCESSING.equals(order.getStatus())) {
            tvConfirm.setVisibility(View.VISIBLE);
            tvReject.setVisibility(View.GONE);
            tvConfirm.setText("Sẵn sàng giao");
            tvConfirm.setOnClickListener(v ->
                    viewModel.updateOrderStatus(order.getId(), Order.STATUS_READY));
        } else if (Order.STATUS_READY.equals(order.getStatus())) {
            tvActionStatus.setVisibility(View.VISIBLE);
            tvActionStatus.setText("✅ Chờ tài xế");
            tvActionStatus.setTextColor(0xFF4CAF50);
        } else {
            tvConfirm.setVisibility(View.GONE);
            tvReject.setVisibility(View.GONE);
        }

        container.addView(card);
    }

    /** Set màu badge trạng thái đơn hàng. */
    private void applyStatusBadge(TextView tvStatus, String status) {
        tvStatus.setText(toVietnameseStatus(status));
        if (Order.STATUS_NEW.equals(status)) {
            tvStatus.setBackgroundResource(R.drawable.bg_status_badge_blue);
        } else if (Order.STATUS_PROCESSING.equals(status)) {
            tvStatus.setBackgroundResource(R.drawable.bg_status_badge_orange);
        } else if (Order.STATUS_READY.equals(status)) {
            tvStatus.setBackgroundResource(R.drawable.bg_status_badge_green);
        } else if (Order.STATUS_DONE.equals(status)) {
            tvStatus.setBackgroundResource(R.drawable.bg_status_badge_green);
        } else {
            // Đã hủy → dùng badge đỏ mặc định
            tvStatus.setBackgroundResource(R.drawable.bg_status_badge);
        }
    }

    private String toVietnameseStatus(String status) {
        if (Order.STATUS_NEW.equals(status)) return "Đơn mới";
        if (Order.STATUS_PROCESSING.equals(status)) return "Đang làm";
        if (Order.STATUS_READY.equals(status)) return "Sẵn sàng giao";
        if (Order.STATUS_DONE.equals(status)) return "Hoàn thành";
        if (Order.STATUS_CANCELLED.equals(status)) return "Đã hủy";
        return status;
    }

    /** Tính top 3 món bán chạy từ đơn hàng hôm nay và bind vào container. */
    private void bindTopFoods(LinearLayout container, TextView tvEmpty, List<Order> todayOrders) {
        // Tổng hợp số lượng + doanh thu theo foodId
        Map<String, int[]> foodStats = new HashMap<>(); // [0]=qty, [1]=nameRef (dùng name), revenue lưu riêng
        Map<String, String> foodNames = new HashMap<>();
        Map<String, String> foodImages = new HashMap<>();
        Map<String, double[]> foodRevenue = new HashMap<>();

        for (Order order : todayOrders) {
            if (order.getItems() == null) continue;
            for (OrderItem item : order.getItems()) {
                String id = item.getFoodId() != null ? item.getFoodId() : item.getTitle();
                int[] stats = foodStats.getOrDefault(id, new int[]{0});
                stats[0] += item.getQuantity();
                foodStats.put(id, stats);
                foodNames.put(id, item.getTitle());
                foodImages.put(id, item.getImageUrl());
                double[] rev = foodRevenue.getOrDefault(id, new double[]{0});
                rev[0] += item.getSubtotal();
                foodRevenue.put(id, rev);
            }
        }

        if (foodStats.isEmpty()) {
            container.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        container.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // Sắp xếp giảm dần theo số lượng
        List<String> sorted = new ArrayList<>(foodStats.keySet());
        Collections.sort(sorted, (a, b) -> foodStats.get(b)[0] - foodStats.get(a)[0]);

        container.removeAllViews();
        int limit = Math.min(3, sorted.size());
        for (int i = 0; i < limit; i++) {
            String id       = sorted.get(i);
            int    qty      = foodStats.get(id)[0];
            double rev      = foodRevenue.getOrDefault(id, new double[]{0})[0];
            String name     = foodNames.get(id);
            String imageUrl = foodImages.get(id);

            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_owner_top_food, container, false);

            ((TextView) item.findViewById(R.id.tv_rank)).setText(String.valueOf(i + 1));
            ((TextView) item.findViewById(R.id.tv_food_name)).setText(name);
            ((TextView) item.findViewById(R.id.tv_food_sold)).setText(String.valueOf(qty));
            ((TextView) item.findViewById(R.id.tv_food_revenue)).setText(formatRevenue(rev));

            container.addView(item);

            // Đường kẻ phân cách (trừ item cuối)
            if (i < limit - 1) {
                View divider = new View(requireContext());
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(0xFFEEEEEE);
                container.addView(divider);
            }
        }
    }

    private void updateOpenStatusUI(TextView tvStatus, TextView tvHint, boolean isOpen) {
        if (isOpen) {
            tvStatus.setText("● Đang mở cửa");
            tvStatus.setTextColor(0xFFAAFFAA);
            tvHint.setText("Nhấn để đóng");
        } else {
            tvStatus.setText("● Đang đóng cửa");
            tvStatus.setTextColor(0xFFFFAAAA);
            tvHint.setText("Nhấn để mở cửa");
        }
    }

    /** Lọc danh sách đơn hàng của ngày hôm nay. */
    private List<Order> filterToday(List<Order> orders) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return orders.stream()
                .filter(o -> o.getCreatedAt() != null &&
                        o.getCreatedAt().toDate().after(today.getTime()))
                .collect(java.util.stream.Collectors.toList());
    }

    /** Tính thời gian tương đối (e.g. "5 phút trước"). */
    private String timeAgo(long timeMs) {
        long diff = System.currentTimeMillis() - timeMs;
        long minutes = diff / 60000;
        if (minutes < 1) return "vừa xong";
        if (minutes < 60) return minutes + " phút trước";
        long hours = minutes / 60;
        if (hours < 24) return hours + " giờ trước";
        return (hours / 24) + " ngày trước";
    }

    private String formatRevenue(double amount) {
        if (amount >= 1_000_000) {
            return String.format(new Locale("vi", "VN"), "%.1ftr", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format(new Locale("vi", "VN"), "%.0fk", amount / 1_000);
        }
        return currFmt.format((long) amount) + "đ";
    }

}

