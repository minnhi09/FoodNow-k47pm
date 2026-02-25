package com.example.foodnow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.adapters.OrderAdapter;
import com.example.foodnow.models.Order;
import com.example.foodnow.viewmodels.OrdersViewModel;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        rvOrders = view.findViewById(R.id.rv_orders);
        tvEmpty  = view.findViewById(R.id.tv_orders_empty);

        orderAdapter = new OrderAdapter(getContext(), orderList);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(orderAdapter);

        OrdersViewModel viewModel = new ViewModelProvider(this).get(OrdersViewModel.class);
        viewModel.getOrders().observe(getViewLifecycleOwner(), orders -> {
            orderList.clear();
            orderList.addAll(orders);
            orderAdapter.notifyDataSetChanged();

            rvOrders.setVisibility(orders.isEmpty() ? View.GONE : View.VISIBLE);
            tvEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
        });

        return view;
    }
}
