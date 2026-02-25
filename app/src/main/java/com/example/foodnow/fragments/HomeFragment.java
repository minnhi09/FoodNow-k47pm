package com.example.foodnow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.adapters.StoreAdapter;
import com.example.foodnow.models.Store;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvStores;
    private StoreAdapter adapter;
    private List<Store> storeList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // ① Inflate: đọc fragment_home.xml → tạo View thật
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // ② Lấy RecyclerView từ layout
        rvStores = view.findViewById(R.id.rv_store_or_food);

        // ③ Tạo dữ liệu giả để test giao diện
        storeList = taoduLieuGia();

        // ④ Tạo Adapter, xử lý sự kiện click
        adapter = new StoreAdapter(getContext(), storeList, store -> {
            Toast.makeText(getContext(),
                    "Bạn chọn: " + store.getName(),
                    Toast.LENGTH_SHORT).show();
        });

        // ⑤ Gắn LayoutManager + Adapter vào RecyclerView
        rvStores.setLayoutManager(new LinearLayoutManager(getContext()));
        rvStores.setAdapter(adapter);

        return view;
    }

    // Dữ liệu giả để xem giao diện — chưa cần Firebase
    private List<Store> taoduLieuGia() {
        List<Store> list = new ArrayList<>();
        list.add(new Store("1", "Phở Hà Nội",    "Quán phở ngon",    "45 Nguyễn Chí Thanh", "0911111111", "", 4.8f, "15 phút", 15000, true));
        list.add(new Store("2", "Pizza Sài Gòn",  "Pizza Ý chính gốc","12 Lê Lợi",           "0922222222", "", 4.5f, "25 phút", 20000, true));
        list.add(new Store("3", "Bún Bò Huế",     "Bún bò truyền thống","78 Trần Phú",       "0933333333", "", 4.7f, "20 phút", 10000, true));
        list.add(new Store("4", "KFC Đà Lạt",     "Gà rán giòn",     "99 Phan Đình Phùng",  "0944444444", "", 4.3f, "30 phút", 25000, true));
        list.add(new Store("5", "Cơm Tấm 3A",     "Cơm tấm sườn bì","33 Hai Bà Trưng",     "0955555555", "", 4.6f, "18 phút", 12000, true));
        list.add(new Store("6", "Lẩu Thái Tâm",   "Lẩu Thái cay",   "56 Bùi Thị Xuân",     "0966666666", "", 4.4f, "35 phút", 18000, true));
        return list;
    }
}
