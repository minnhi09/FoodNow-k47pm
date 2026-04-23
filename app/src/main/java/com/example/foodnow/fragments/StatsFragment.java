package com.example.foodnow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodnow.R;
import com.example.foodnow.activities.StoreOwnerActivity;
import com.example.foodnow.models.Order;
import com.example.foodnow.models.OrderItem;
import com.example.foodnow.viewmodels.StoreOwnerViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Tab "Thống kê" — doanh thu, đơn hàng, top món bán chạy theo khoảng thời gian. */
public class StatsFragment extends Fragment {

    // Filter: 0=Hôm nay, 1=Tuần này, 2=Tháng này
    private int currentPeriod = 0;

    private StoreOwnerViewModel viewModel;
    private List<Order>         allOrders = new ArrayList<>();
    private NumberFormat        currFmt;

    private TextView tvStoreName, tvRevenue, tvOrders, tvRating, tvCustomers;
    private TextView tabToday, tabWeek, tabMonth;
    private LinearLayout containerTopFoods;
    private TextView tvNoStats;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String storeId = ((StoreOwnerActivity) requireActivity()).getStoreId();
        viewModel = new ViewModelProvider(requireActivity()).get(StoreOwnerViewModel.class);
        currFmt   = NumberFormat.getInstance(new Locale("vi", "VN"));

        tvStoreName        = view.findViewById(R.id.tv_stats_store_name);
        tvRevenue          = view.findViewById(R.id.tv_stat_revenue);
        tvOrders           = view.findViewById(R.id.tv_stat_orders);
        tvRating           = view.findViewById(R.id.tv_stat_rating);
        tvCustomers        = view.findViewById(R.id.tv_stat_customers);
        tabToday           = view.findViewById(R.id.tab_today);
        tabWeek            = view.findViewById(R.id.tab_week);
        tabMonth           = view.findViewById(R.id.tab_month);
        containerTopFoods  = view.findViewById(R.id.container_top_foods);
        tvNoStats          = view.findViewById(R.id.tv_no_stats);

        tabToday.setOnClickListener(v -> setPeriod(0));
        tabWeek.setOnClickListener(v  -> setPeriod(1));
        tabMonth.setOnClickListener(v -> setPeriod(2));

        // Store name
        viewModel.getStore(storeId).observe(getViewLifecycleOwner(), store -> {
            if (store != null) tvStoreName.setText(store.getName());
        });

        // Orders data
        viewModel.getOrders(storeId).observe(getViewLifecycleOwner(), orders -> {
            allOrders = orders != null ? orders : new ArrayList<>();
            renderStats();
        });
    }

    private void setPeriod(int period) {
        currentPeriod = period;
        updateTabUI();
        renderStats();
    }

    private void renderStats() {
        List<Order> periodOrders = filterByPeriod(allOrders, currentPeriod);
        List<Order> doneOrders   = periodOrders.stream()
                .filter(o -> Order.STATUS_DONE.equals(o.getStatus()))
                .collect(Collectors.toList());

        // Doanh thu (chỉ đơn Hoàn thành)
        double revenue = doneOrders.stream().mapToDouble(Order::getTotal).sum();
        tvRevenue.setText(currFmt.format((long) revenue) + "đ");

        // Số đơn hoàn thành
        tvOrders.setText(String.valueOf(doneOrders.size()));

        // Khách hàng (unique userId)
        long customers = periodOrders.stream()
                .map(Order::getUserId).distinct().count();
        tvCustomers.setText(String.valueOf(customers));

        // Rating TB (từ store, vì Orders không có rating)
        tvRating.setText("—");

        // Top món bán chạy
        buildTopFoods(doneOrders);
    }

    private void buildTopFoods(List<Order> doneOrders) {
        // Tổng hợp số lượng + doanh thu theo foodId
        Map<String, String>  foodNames    = new HashMap<>();
        Map<String, Integer> foodSold     = new HashMap<>();
        Map<String, Double>  foodRevenue  = new HashMap<>();

        for (Order order : doneOrders) {
            if (order.getItems() == null) continue;
            for (OrderItem item : order.getItems()) {
                String id = item.getFoodId();
                foodNames.put(id, item.getTitle());
                foodSold.put(id,    foodSold.getOrDefault(id, 0)    + item.getQuantity());
                foodRevenue.put(id, foodRevenue.getOrDefault(id, 0.0) + item.getSubtotal());
            }
        }

        // Sắp xếp giảm dần theo số lượng bán
        List<String> sorted = new ArrayList<>(foodSold.keySet());
        sorted.sort((a, b) -> foodSold.get(b) - foodSold.get(a));

        containerTopFoods.removeAllViews();

        if (sorted.isEmpty()) {
            tvNoStats.setVisibility(View.VISIBLE);
            return;
        }
        tvNoStats.setVisibility(View.GONE);

        int rank = 1;
        for (String id : sorted) {
            if (rank > 5) break;  // top 5
            View row = buildTopFoodRow(rank, foodNames.get(id),
                    foodSold.get(id), foodRevenue.get(id));
            containerTopFoods.addView(row);
            rank++;
        }
    }

    private View buildTopFoodRow(int rank, String name, int sold, double revenue) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, (int)(8 * getResources().getDisplayMetrics().density));
        row.setLayoutParams(rowParams);

        int dp = (int) getResources().getDisplayMetrics().density;

        // Rank
        TextView tvRank = new TextView(requireContext());
        tvRank.setLayoutParams(new LinearLayout.LayoutParams(32 * dp, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvRank.setText(String.valueOf(rank));
        tvRank.setTextSize(13);
        tvRank.setTextColor(rank == 1 ? 0xFFFF6B35 : 0xFF757575);
        tvRank.setTypeface(null, rank == 1 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        row.addView(tvRank);

        // Name
        TextView tvName = new TextView(requireContext());
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tvName.setLayoutParams(nameParams);
        tvName.setText(name != null ? name : "—");
        tvName.setTextSize(13);
        tvName.setTextColor(0xFF212121);
        tvName.setMaxLines(1);
        tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        row.addView(tvName);

        // Sold count
        TextView tvSold = new TextView(requireContext());
        tvSold.setLayoutParams(new LinearLayout.LayoutParams(60 * dp, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvSold.setText(sold + " phần");
        tvSold.setTextSize(12);
        tvSold.setTextColor(0xFF424242);
        tvSold.setGravity(android.view.Gravity.END);
        row.addView(tvSold);

        // Revenue
        TextView tvRev = new TextView(requireContext());
        tvRev.setLayoutParams(new LinearLayout.LayoutParams(80 * dp, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvRev.setText(currFmt.format((long) revenue) + "đ");
        tvRev.setTextSize(12);
        tvRev.setTextColor(0xFFFF6B35);
        tvRev.setGravity(android.view.Gravity.END);
        row.addView(tvRev);

        return row;
    }

    private List<Order> filterByPeriod(List<Order> orders, int period) {
        Calendar from = Calendar.getInstance();
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        from.set(Calendar.MILLISECOND, 0);

        if (period == 1) {
            // Tuần này — về đầu tuần (Monday)
            from.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        } else if (period == 2) {
            // Tháng này — về đầu tháng
            from.set(Calendar.DAY_OF_MONTH, 1);
        }

        final long fromMs = from.getTimeInMillis();
        return orders.stream()
                .filter(o -> o.getCreatedAt() != null
                        && o.getCreatedAt().toDate().getTime() >= fromMs)
                .collect(Collectors.toList());
    }

    private void updateTabUI() {
        setTabSelected(tabToday, currentPeriod == 0);
        setTabSelected(tabWeek,  currentPeriod == 1);
        setTabSelected(tabMonth, currentPeriod == 2);
    }

    private void setTabSelected(TextView tab, boolean selected) {
        if (selected) {
            tab.setBackgroundResource(R.drawable.bg_tab_selected);
            tab.setTextColor(0xFFFFFFFF);
        } else {
            tab.setBackgroundResource(R.drawable.bg_tab_unselected);
            tab.setTextColor(0xFFFF6B35);
        }
    }
}

