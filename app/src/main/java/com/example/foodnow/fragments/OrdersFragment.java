package com.example.foodnow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.adapters.OrderAdapter;
import com.example.foodnow.models.Order;
import com.example.foodnow.viewmodels.OrdersViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private TextView tvOrdersEmpty, tvOrderCountHeader;
    private TextView tabAll, tabProcessing, tabCompleted;
    private OrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private final List<Order> displayList = new ArrayList<>();
    private String currentFilter = "ALL";

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

        rvOrders = view.findViewById(R.id.rv_orders);
        tvOrdersEmpty = view.findViewById(R.id.tv_orders_empty);
        tvOrderCountHeader = view.findViewById(R.id.tv_order_count_header);
        
        tabAll = view.findViewById(R.id.tab_all);
        tabProcessing = view.findViewById(R.id.tab_processing);
        tabCompleted = view.findViewById(R.id.tab_completed);

        adapter = new OrderAdapter(requireContext(), displayList);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(adapter);

        setupTabs();

        OrdersViewModel viewModel = new ViewModelProvider(this).get(OrdersViewModel.class);
        
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            viewModel.fetchOrders(user.getUid());
        }

        viewModel.getOrdersLiveData().observe(getViewLifecycleOwner(), orders -> {
            allOrders = orders != null ? orders : new ArrayList<>();
            tvOrderCountHeader.setText(allOrders.size() + " đơn hàng");
            filterOrders();
        });
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateTabUi();
            filterOrders();
        });

        tabProcessing.setOnClickListener(v -> {
            currentFilter = "PROCESSING";
            updateTabUi();
            filterOrders();
        });

        tabCompleted.setOnClickListener(v -> {
            currentFilter = "COMPLETED";
            updateTabUi();
            filterOrders();
        });
    }

    private void updateTabUi() {
        // Reset all tabs
        resetTab(tabAll);
        resetTab(tabProcessing);
        resetTab(tabCompleted);

        // Highlight selected tab
        TextView selected = null;
        if (currentFilter.equals("ALL")) selected = tabAll;
        else if (currentFilter.equals("PROCESSING")) selected = tabProcessing;
        else if (currentFilter.equals("COMPLETED")) selected = tabCompleted;

        if (selected != null) {
            selected.setBackgroundResource(R.drawable.bg_order_tab_indicator);
            selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
    }

    private void resetTab(TextView tab) {
        tab.setBackground(null);
        tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.home_text_secondary));
    }

    private void filterOrders() {
        displayList.clear();
        if (currentFilter.equals("ALL")) {
            displayList.addAll(allOrders);
        } else if (currentFilter.equals("PROCESSING")) {
            for (Order o : allOrders) {
                if ("Đang xử lý".equalsIgnoreCase(o.getStatus()) || "Đang giao".equalsIgnoreCase(o.getStatus())) {
                    displayList.add(o);
                }
            }
        } else if (currentFilter.equals("COMPLETED")) {
            for (Order o : allOrders) {
                if ("Hoàn thành".equalsIgnoreCase(o.getStatus())) {
                    displayList.add(o);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        boolean isEmpty = displayList.isEmpty();
        tvOrdersEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvOrders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
