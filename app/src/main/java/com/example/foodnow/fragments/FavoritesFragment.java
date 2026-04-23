package com.example.foodnow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.activities.FoodDetailActivity;
import com.example.foodnow.activities.StoreDetailActivity;
import com.example.foodnow.adapters.FavoriteAdapter;
import com.example.foodnow.models.Favorite;
import com.example.foodnow.viewmodels.FavoritesViewModel;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private TextView tvFavoritesEmpty, tvFavoriteCountHeader;
    private TextView tabAll, tabStores, tabFoods;
    private FavoriteAdapter adapter;
    private List<Favorite> allFavorites = new ArrayList<>();
    private final List<Favorite> displayList = new ArrayList<>();
    private String currentFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFavorites = view.findViewById(R.id.rv_favorites);
        tvFavoritesEmpty = view.findViewById(R.id.tv_favorites_empty);
        tvFavoriteCountHeader = view.findViewById(R.id.tv_favorite_count_header);
        
        tabAll = view.findViewById(R.id.tab_fav_all);
        tabStores = view.findViewById(R.id.tab_fav_stores);
        tabFoods = view.findViewById(R.id.tab_fav_foods);

        FavoritesViewModel viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);

        adapter = new FavoriteAdapter(requireContext(), new FavoriteAdapter.OnFavoriteListener() {
            @Override
            public void onRemove(Favorite favorite) {
                if (favorite.getId() != null) {
                    viewModel.removeFavorite(favorite.getId());
                    Toast.makeText(getContext(), "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onClick(Favorite favorite) {
                if ("store".equalsIgnoreCase(favorite.getType())) {
                    Intent intent = new Intent(getContext(), StoreDetailActivity.class);
                    intent.putExtra("storeId", favorite.getItemId());
                    intent.putExtra("storeName", favorite.getName());
                    intent.putExtra("storeImage", favorite.getImageUrl());
                    startActivity(intent);
                } else if ("food".equalsIgnoreCase(favorite.getType())) {
                    Intent intent = new Intent(getContext(), FoodDetailActivity.class);
                    intent.putExtra("foodId", favorite.getItemId());
                    intent.putExtra("foodTitle", favorite.getName());
                    intent.putExtra("foodImageUrl", favorite.getImageUrl());
                    // Lưu ý: Các thông tin khác như giá, mô tả sẽ cần fetch lại ở FoodDetail hoặc pass thêm nếu có
                    startActivity(intent);
                }
            }
        });

        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFavorites.setAdapter(adapter);

        setupTabs();

        viewModel.getFavorites().observe(getViewLifecycleOwner(), favorites -> {
            allFavorites = favorites != null ? favorites : new ArrayList<>();
            if (tvFavoriteCountHeader != null) {
                tvFavoriteCountHeader.setText(allFavorites.size() + " mục đã lưu");
            }
            filterFavorites();
        });
    }

    private void setupTabs() {
        if (tabAll == null) return;
        tabAll.setOnClickListener(v -> { currentFilter = "ALL"; updateTabUi(); filterFavorites(); });
        tabStores.setOnClickListener(v -> { currentFilter = "STORE"; updateTabUi(); filterFavorites(); });
        tabFoods.setOnClickListener(v -> { currentFilter = "FOOD"; updateTabUi(); filterFavorites(); });
    }

    private void updateTabUi() {
        resetTab(tabAll);
        resetTab(tabStores);
        resetTab(tabFoods);

        TextView selected = null;
        if (currentFilter.equals("ALL")) selected = tabAll;
        else if (currentFilter.equals("STORE")) selected = tabStores;
        else if (currentFilter.equals("FOOD")) selected = tabFoods;

        if (selected != null) {
            selected.setBackgroundResource(R.drawable.bg_order_tab_indicator);
            selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
    }

    private void resetTab(TextView tab) {
        if (tab == null) return;
        tab.setBackground(null);
        tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.home_text_secondary));
    }

    private void filterFavorites() {
        displayList.clear();
        if (currentFilter.equals("ALL")) {
            displayList.addAll(allFavorites);
        } else {
            for (Favorite f : allFavorites) {
                if (currentFilter.equalsIgnoreCase(f.getType())) {
                    displayList.add(f);
                }
            }
        }
        
        adapter.submitList(displayList);
        boolean isEmpty = displayList.isEmpty();
        tvFavoritesEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvFavorites.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
