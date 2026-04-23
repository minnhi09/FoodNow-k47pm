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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.adapters.CustomerOrderAdapter;
import com.example.foodnow.models.Order;
import com.example.foodnow.repositories.OrderRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrdersFragment extends Fragment {
    private static final int FILTER_ALL = 0;
    private static final int FILTER_ACTIVE = 1;
    private static final int FILTER_DONE = 2;

    private int currentFilter = FILTER_ALL;
    private List<Order> allOrders = new ArrayList<>();

    private CustomerOrderAdapter adapter;
    private TextView tvCount;
    private TextView tabAll;
    private TextView tabActive;
    private TextView tabDone;
    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;

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

        tvCount = view.findViewById(R.id.tv_customer_order_count);
        tabAll = view.findViewById(R.id.tab_customer_all);
        tabActive = view.findViewById(R.id.tab_customer_active);
        tabDone = view.findViewById(R.id.tab_customer_done);
        rvOrders = view.findViewById(R.id.rv_customer_orders);
        layoutEmpty = view.findViewById(R.id.layout_customer_orders_empty);

        adapter = new CustomerOrderAdapter();
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

        new OrderRepository().getOrdersByUser(uid).observe(getViewLifecycleOwner(), orders -> {
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
        setTabStyle(tabAll, currentFilter == FILTER_ALL);
        setTabStyle(tabActive, currentFilter == FILTER_ACTIVE);
        setTabStyle(tabDone, currentFilter == FILTER_DONE);
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
}
