package com.example.foodnow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.activities.StoreDetailActivity;
import com.example.foodnow.adapters.CategoryAdapter;
import com.example.foodnow.adapters.StoreAdapter;
import com.example.foodnow.models.Category;
import com.example.foodnow.models.Store;
import com.example.foodnow.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvStores, rvCategories;
    private StoreAdapter storeAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Store> storeList = new ArrayList<>();
    private List<Store> allStores = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private HomeViewModel homeViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ RecyclerView
        rvStores     = view.findViewById(R.id.rv_store_or_food);
        rvCategories = view.findViewById(R.id.rv_categories);
        SearchView svSearch = view.findViewById(R.id.sv_search);

        // Setup danh mục (RecyclerView ngang)
        categoryAdapter = new CategoryAdapter(getContext(), categoryList, category -> {
            Toast.makeText(getContext(), "Danh mục: " + category.getName(), Toast.LENGTH_SHORT).show();
        });
        rvCategories.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Setup danh sách quán
        storeAdapter = new StoreAdapter(getContext(), storeList, store -> {
            // Mở chi tiết quán
            Intent intent = new Intent(getContext(), StoreDetailActivity.class);
            intent.putExtra(StoreDetailActivity.EXTRA_STORE_ID, store.getId());
            intent.putExtra(StoreDetailActivity.EXTRA_STORE_NAME, store.getName());
            intent.putExtra(StoreDetailActivity.EXTRA_STORE_DESC, store.getDescription());
            intent.putExtra(StoreDetailActivity.EXTRA_STORE_IMG, store.getImageUrl());
            intent.putExtra(StoreDetailActivity.EXTRA_STORE_RATING, store.getRating());
            intent.putExtra(StoreDetailActivity.EXTRA_STORE_TIME, store.getDeliveryTime());
            startActivity(intent);
        });
        rvStores.setLayoutManager(new LinearLayoutManager(getContext()));
        rvStores.setAdapter(storeAdapter);

        // Khởi tạo ViewModel và quan sát dữ liệu
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.getStores().observe(getViewLifecycleOwner(), stores -> {
            allStores.clear();
            allStores.addAll(stores);
            storeList.clear();
            storeList.addAll(stores);
            storeAdapter.notifyDataSetChanged();
        });

        homeViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryList.clear();
            categoryList.addAll(categories);
            categoryAdapter.notifyDataSetChanged();
        });

        // Tìm kiếm quán theo tên
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterStores(newText);
                return true;
            }
        });

        return view;
    }

    /** Lọc danh sách quán theo keyword */
    private void filterStores(String keyword) {
        storeList.clear();
        if (keyword == null || keyword.isEmpty()) {
            storeList.addAll(allStores);
        } else {
            String lower = keyword.toLowerCase();
            for (Store store : allStores) {
                if (store.getName() != null && store.getName().toLowerCase().contains(lower)) {
                    storeList.add(store);
                }
            }
        }
        storeAdapter.notifyDataSetChanged();
    }
}
