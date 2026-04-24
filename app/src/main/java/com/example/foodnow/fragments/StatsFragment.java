package com.example.foodnow.fragments;

import android.graphics.Color;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Tab "Thống kê" — doanh thu, đơn hàng, biểu đồ, cơ cấu doanh thu, top món bán chạy. */
public class StatsFragment extends Fragment {

    // Filter: 0=Hôm nay, 1=Tuần này, 2=Tháng này
    private int currentPeriod = 1;
    // Chart type: true=Doanh thu, false=Đơn hàng
    private boolean showRevenue = true;

    private StoreOwnerViewModel viewModel;
    private List<Order>         allOrders = new ArrayList<>();
    private NumberFormat        currFmt;

    private TextView tvStoreName, tvRevenue, tvOrders, tvRating, tvCustomers;
    private TextView tvRevenueChange, tvOrdersChange, tvRatingChange, tvCustomersChange;
    private TextView tabToday, tabWeek, tabMonth;
    private TextView tvChartRevenue, tvChartOrders;
    private LinearLayout containerTopFoods, containerPieLegend;
    private TextView tvNoStats;
    private LineChart lineChart;
    private PieChart  pieChart;

    // Màu cho biểu đồ tròn
    private static final int[] PIE_COLORS = {
        0xFFBF360C, 0xFFE64A19, 0xFFFF5722, 0xFFFF8A65, 0xFFFFCCBC
    };

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

        tvStoreName       = view.findViewById(R.id.tv_stats_store_name);
        tvRevenue         = view.findViewById(R.id.tv_stat_revenue);
        tvOrders          = view.findViewById(R.id.tv_stat_orders);
        tvRating          = view.findViewById(R.id.tv_stat_rating);
        tvCustomers       = view.findViewById(R.id.tv_stat_customers);
        tvRevenueChange   = view.findViewById(R.id.tv_stat_revenue_change);
        tvOrdersChange    = view.findViewById(R.id.tv_stat_orders_change);
        tvRatingChange    = view.findViewById(R.id.tv_stat_rating_change);
        tvCustomersChange = view.findViewById(R.id.tv_stat_customers_change);
        tabToday          = view.findViewById(R.id.tab_today);
        tabWeek           = view.findViewById(R.id.tab_week);
        tabMonth          = view.findViewById(R.id.tab_month);
        tvChartRevenue    = view.findViewById(R.id.tv_chart_type_revenue);
        tvChartOrders     = view.findViewById(R.id.tv_chart_type_orders);
        containerTopFoods = view.findViewById(R.id.container_top_foods);
        containerPieLegend = view.findViewById(R.id.container_pie_legend);
        tvNoStats         = view.findViewById(R.id.tv_no_stats);
        lineChart         = view.findViewById(R.id.line_chart);
        pieChart          = view.findViewById(R.id.pie_chart);

        setupLineChartStyle();
        setupPieChartStyle();

        tabToday.setOnClickListener(v  -> setPeriod(0));
        tabWeek.setOnClickListener(v   -> setPeriod(1));
        tabMonth.setOnClickListener(v  -> setPeriod(2));

        tvChartRevenue.setOnClickListener(v -> setChartType(true));
        tvChartOrders.setOnClickListener(v  -> setChartType(false));

        viewModel.getStore(storeId).observe(getViewLifecycleOwner(), store -> {
            if (store != null) tvStoreName.setText(store.getName());
        });

        viewModel.getOrders(storeId).observe(getViewLifecycleOwner(), orders -> {
            allOrders = orders != null ? orders : new ArrayList<>();
            renderStats();
        });

        updateTabUI();
    }

    private void setPeriod(int period) {
        currentPeriod = period;
        updateTabUI();
        renderStats();
    }

    private void setChartType(boolean revenue) {
        showRevenue = revenue;
        if (revenue) {
            tvChartRevenue.setBackgroundResource(R.drawable.bg_tab_selected);
            tvChartRevenue.setTextColor(0xFFFFFFFF);
            tvChartOrders.setBackgroundColor(Color.TRANSPARENT);
            tvChartOrders.setTextColor(0xFFFF6B35);
        } else {
            tvChartOrders.setBackgroundResource(R.drawable.bg_tab_selected);
            tvChartOrders.setTextColor(0xFFFFFFFF);
            tvChartRevenue.setBackgroundColor(Color.TRANSPARENT);
            tvChartRevenue.setTextColor(0xFFFF6B35);
        }
        renderLineChart(filterByPeriod(allOrders, currentPeriod));
    }

    private void renderStats() {
        List<Order> periodOrders = filterByPeriod(allOrders, currentPeriod);
        List<Order> prevOrders   = filterPreviousPeriod(allOrders, currentPeriod);

        List<Order> doneOrders     = filterDone(periodOrders);
        List<Order> prevDoneOrders = filterDone(prevOrders);

        // Doanh thu
        double revenue     = doneOrders.stream().mapToDouble(Order::getTotal).sum();
        double prevRevenue = prevDoneOrders.stream().mapToDouble(Order::getTotal).sum();
        tvRevenue.setText(formatRevenue(revenue));
        tvRevenueChange.setText(formatChange(revenue, prevRevenue, false));
        colorChangeText(tvRevenueChange, revenue, prevRevenue);

        // Đơn hàng
        int orderCount = doneOrders.size();
        int prevCount  = prevDoneOrders.size();
        tvOrders.setText(String.valueOf(orderCount));
        tvOrdersChange.setText(formatChange(orderCount, prevCount, false));
        colorChangeText(tvOrdersChange, orderCount, prevCount);

        // Khách hàng (unique userId trong kỳ)
        long customers = periodOrders.stream().map(Order::getUserId).distinct().count();
        long prevCustomers = prevOrders.stream().map(Order::getUserId).distinct().count();
        tvCustomers.setText(String.valueOf(customers));
        tvCustomersChange.setText(formatChange(customers, prevCustomers, false));
        colorChangeText(tvCustomersChange, customers, prevCustomers);

        // Rating — không có dữ liệu từ Orders
        tvRating.setText("—");
        tvRatingChange.setText("—");
        tvRatingChange.setTextColor(0xFF9E9E9E);

        renderLineChart(periodOrders);
        renderPieChart(doneOrders);
        buildTopFoods(doneOrders);
    }

    // ===== Line Chart =====

    private void setupLineChartStyle() {
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setTextColor(0xFF9E9E9E);
        lineChart.getAxisLeft().setGridColor(0xFFEEEEEE);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setTextColor(0xFF9E9E9E);
        lineChart.getXAxis().setGridColor(0xFFEEEEEE);
        lineChart.getXAxis().setDrawAxisLine(false);
        lineChart.setExtraBottomOffset(8f);
    }

    private void renderLineChart(List<Order> periodOrders) {
        List<Order> doneOrders = filterDone(periodOrders);
        String[] labels;
        int buckets;

        if (currentPeriod == 0) {
            // Hôm nay: 24 giờ → nhóm theo 4 giờ: 0-4, 4-8, 8-12, 12-16, 16-20, 20-24
            labels  = new String[]{"0h", "4h", "8h", "12h", "16h", "20h", "24h"};
            buckets = 7;
        } else if (currentPeriod == 1) {
            // Tuần: T2-CN (7 ngày)
            labels  = new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
            buckets = 7;
        } else {
            // Tháng: 4 tuần
            labels  = new String[]{"T1", "T2", "T3", "T4"};
            buckets = 4;
        }

        float[] values = new float[buckets];
        Calendar now = Calendar.getInstance();

        for (Order o : doneOrders) {
            if (o.getCreatedAt() == null) continue;
            Calendar c = Calendar.getInstance();
            c.setTime(o.getCreatedAt().toDate());

            int bucket;
            if (currentPeriod == 0) {
                bucket = Math.min(c.get(Calendar.HOUR_OF_DAY) / 4, buckets - 1);
            } else if (currentPeriod == 1) {
                int dow = c.get(Calendar.DAY_OF_WEEK); // Sun=1, Mon=2 ... Sat=7
                bucket = (dow == Calendar.SUNDAY) ? 6 : dow - 2; // Mon=0..Sat=5,Sun=6
            } else {
                int day = c.get(Calendar.DAY_OF_MONTH);
                bucket = Math.min((day - 1) / 7, buckets - 1);
            }

            if (bucket >= 0 && bucket < buckets) {
                values[bucket] += showRevenue ? (float) o.getTotal() : 1f;
            }
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < buckets; i++) entries.add(new Entry(i, values[i]));

        LineDataSet ds = new LineDataSet(entries, "");
        ds.setColor(0xFFFF6B35);
        ds.setCircleColor(0xFFFF6B35);
        ds.setCircleRadius(4f);
        ds.setLineWidth(2f);
        ds.setDrawValues(false);
        ds.setDrawFilled(true);
        ds.setFillColor(0xFFFF6B35);
        ds.setFillAlpha(40);
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setLabelCount(buckets);

        if (showRevenue) {
            lineChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    if (value == 0) return "0";
                    return (int)(value / 1_000_000) + "tr";
                }
            });
        } else {
            lineChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });
        }

        lineChart.setData(new LineData(ds));
        lineChart.invalidate();
    }

    // ===== Pie Chart =====

    private void setupPieChartStyle() {
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setDrawEntryLabels(false);
        pieChart.setTouchEnabled(false);
        pieChart.setRotationEnabled(false);
    }

    private void renderPieChart(List<Order> doneOrders) {
        Map<String, String> foodNames   = new HashMap<>();
        Map<String, Double> foodRevenue = new HashMap<>();

        for (Order order : doneOrders) {
            if (order.getItems() == null) continue;
            for (OrderItem item : order.getItems()) {
                String id = item.getFoodId();
                foodNames.put(id, item.getTitle());
                foodRevenue.put(id, foodRevenue.getOrDefault(id, 0.0) + item.getSubtotal());
            }
        }

        if (foodRevenue.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            containerPieLegend.removeAllViews();
            return;
        }

        // Sắp xếp giảm dần theo doanh thu
        List<String> sorted = new ArrayList<>(foodRevenue.keySet());
        sorted.sort((a, b) -> Double.compare(foodRevenue.get(b), foodRevenue.get(a)));

        List<PieEntry> entries = new ArrayList<>();
        containerPieLegend.removeAllViews();

        double total = foodRevenue.values().stream().mapToDouble(d -> d).sum();
        double others = 0;

        for (int i = 0; i < sorted.size(); i++) {
            String id  = sorted.get(i);
            double rev = foodRevenue.get(id);
            String name = foodNames.get(id) != null ? foodNames.get(id) : id;

            if (i < 4) {
                entries.add(new PieEntry((float) rev, name));
                addPieLegendRow(name, (int)(rev / 1000), PIE_COLORS[i]);
            } else {
                others += rev;
            }
        }

        if (others > 0) {
            entries.add(new PieEntry((float) others, "Khác"));
            addPieLegendRow("Khác", (int)(others / 1000), PIE_COLORS[4]);
        }

        PieDataSet ds = new PieDataSet(entries, "");
        int[] colors = new int[entries.size()];
        for (int i = 0; i < colors.length; i++) colors[i] = PIE_COLORS[Math.min(i, PIE_COLORS.length - 1)];
        ds.setColors(colors);
        ds.setSliceSpace(2f);
        ds.setDrawValues(false);

        pieChart.setData(new PieData(ds));
        pieChart.invalidate();
    }

    private void addPieLegendRow(String name, int value, int color) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int dp = (int) getResources().getDisplayMetrics().density;
        rp.setMargins(0, 0, 0, 4 * dp);
        row.setLayoutParams(rp);

        // Color dot
        View dot = new View(requireContext());
        dot.setLayoutParams(new LinearLayout.LayoutParams(10 * dp, 10 * dp));
        dot.setBackgroundColor(color);
        row.addView(dot);

        // Name
        TextView tvName = new TextView(requireContext());
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        np.setMarginStart(6 * dp);
        tvName.setLayoutParams(np);
        tvName.setText(name);
        tvName.setTextSize(12);
        tvName.setTextColor(0xFF424242);
        tvName.setMaxLines(1);
        tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        row.addView(tvName);

        // Value
        TextView tvVal = new TextView(requireContext());
        tvVal.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvVal.setText(String.valueOf(value));
        tvVal.setTextSize(12);
        tvVal.setTextColor(0xFF757575);
        row.addView(tvVal);

        containerPieLegend.addView(row);
    }

    // ===== Top Foods =====

    private void buildTopFoods(List<Order> doneOrders) {
        Map<String, String>  foodNames   = new HashMap<>();
        Map<String, Integer> foodSold    = new HashMap<>();
        Map<String, Double>  foodRevenue = new HashMap<>();

        for (Order order : doneOrders) {
            if (order.getItems() == null) continue;
            for (OrderItem item : order.getItems()) {
                String id = item.getFoodId();
                foodNames.put(id, item.getTitle());
                foodSold.put(id, foodSold.getOrDefault(id, 0) + item.getQuantity());
                foodRevenue.put(id, foodRevenue.getOrDefault(id, 0.0) + item.getSubtotal());
            }
        }

        List<String> sorted = new ArrayList<>(foodSold.keySet());
        sorted.sort((a, b) -> foodSold.get(b) - foodSold.get(a));

        containerTopFoods.removeAllViews();

        if (sorted.isEmpty()) {
            tvNoStats.setVisibility(View.VISIBLE);
            return;
        }
        tvNoStats.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int rank = 1;
        for (String id : sorted) {
            if (rank > 5) break;
            View row = inflater.inflate(R.layout.item_owner_top_food, containerTopFoods, false);

            TextView tvRank    = row.findViewById(R.id.tv_rank);
            TextView tvName    = row.findViewById(R.id.tv_food_name);
            TextView tvSold    = row.findViewById(R.id.tv_food_sold);
            TextView tvRevView = row.findViewById(R.id.tv_food_revenue);

            tvRank.setText(String.valueOf(rank));
            tvRank.setTextColor(rank == 1 ? 0xFFFF6B35 : 0xFF757575);
            tvName.setText(foodNames.get(id) != null ? foodNames.get(id) : "—");
            tvSold.setText(String.valueOf(foodSold.get(id)));
            tvRevView.setText(formatRevenue(foodRevenue.get(id)));

            containerTopFoods.addView(row);

            // Divider (except after last)
            if (rank < Math.min(sorted.size(), 5)) {
                View divider = new View(requireContext());
                LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(dp);
                divider.setBackgroundColor(0xFFF0F0F0);
                containerTopFoods.addView(divider);
            }

            rank++;
        }
    }

    // ===== Helpers =====

    private List<Order> filterDone(List<Order> orders) {
        return orders.stream()
                .filter(o -> Order.STATUS_DONE.equals(o.getStatus()))
                .collect(Collectors.toList());
    }

    private List<Order> filterByPeriod(List<Order> orders, int period) {
        Calendar from = Calendar.getInstance();
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        from.set(Calendar.MILLISECOND, 0);

        if (period == 1) from.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        else if (period == 2) from.set(Calendar.DAY_OF_MONTH, 1);

        final long fromMs = from.getTimeInMillis();
        return orders.stream()
                .filter(o -> o.getCreatedAt() != null
                        && o.getCreatedAt().toDate().getTime() >= fromMs)
                .collect(Collectors.toList());
    }

    /** Kỳ liền trước cùng độ dài (để tính % thay đổi). */
    private List<Order> filterPreviousPeriod(List<Order> orders, int period) {
        Calendar toDate = Calendar.getInstance();
        toDate.set(Calendar.HOUR_OF_DAY, 0);
        toDate.set(Calendar.MINUTE, 0);
        toDate.set(Calendar.SECOND, 0);
        toDate.set(Calendar.MILLISECOND, 0);

        if (period == 1) toDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        else if (period == 2) toDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar fromDate = (Calendar) toDate.clone();
        if (period == 0) fromDate.add(Calendar.DAY_OF_YEAR, -1);
        else if (period == 1) fromDate.add(Calendar.WEEK_OF_YEAR, -1);
        else fromDate.add(Calendar.MONTH, -1);

        final long fromMs = fromDate.getTimeInMillis();
        final long toMs   = toDate.getTimeInMillis();
        return orders.stream()
                .filter(o -> o.getCreatedAt() != null) 
                .filter(o -> {
                    long t = o.getCreatedAt().toDate().getTime();
                    return t >= fromMs && t < toMs;
                })
                .collect(Collectors.toList());
    }

    private String formatRevenue(double amount) {
        if (amount >= 1_000_000) {
            return String.format(Locale.getDefault(), "%.1ftr", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format(Locale.getDefault(), "%.0fk", amount / 1_000);
        }
        return currFmt.format((long) amount) + "đ";
    }

    private String formatChange(double current, double prev, boolean isRating) {
        if (prev == 0) return current > 0 ? "↑ mới" : "—";
        double diff = current - prev;
        if (isRating) {
            return (diff >= 0 ? "↑ +" : "↓ ") + String.format(Locale.getDefault(), "%.1f", diff);
        }
        double pct = (diff / prev) * 100;
        return (diff >= 0 ? "↑ +" : "↓ ") + String.format(Locale.getDefault(), "%.0f%%", Math.abs(pct));
    }

    private void colorChangeText(TextView tv, double current, double prev) {
        if (current >= prev) tv.setTextColor(0xFF4CAF50);
        else tv.setTextColor(0xFFF44336);
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

