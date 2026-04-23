package com.example.foodnow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodnow.R;

/**
 * Tab "Đơn hàng" trong StoreOwnerActivity.
 * Danh sách đơn hàng đặt đến quán của chủ nhà hàng.
 * Placeholder — sẽ mở rộng khi TV3 hoàn thành Orders flow.
 */
public class StoreOrdersFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_store_orders, container, false);
    }
}
