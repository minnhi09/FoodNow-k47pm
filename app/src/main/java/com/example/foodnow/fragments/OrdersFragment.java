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
    private TextView tvOrdersEmpty;
    private OrderAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();

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

        adapter = new OrderAdapter(requireContext(), orderList);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(adapter);

        OrdersViewModel viewModel = new ViewModelProvider(this).get(OrdersViewModel.class);
        viewModel.getOrders().observe(getViewLifecycleOwner(), orders -> {
            orderList.clear();
            if (orders != null) {
                orderList.addAll(orders);
            }
            adapter.notifyDataSetChanged();

            boolean isEmpty = orderList.isEmpty();
            tvOrdersEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvOrders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }
}
