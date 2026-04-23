package com.example.foodnow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.activities.StoreDetailActivity;
import com.example.foodnow.adapters.FavoriteStoreAdapter;
import com.example.foodnow.models.Favorite;
import com.example.foodnow.repositories.FavoriteRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private FavoriteStoreAdapter adapter;
    private FavoriteRepository favoriteRepository;
    private TextView tvCount;
    private RecyclerView rvFavorites;
    private LinearLayout layoutEmpty;

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

        tvCount      = view.findViewById(R.id.tv_favorites_count);
        rvFavorites  = view.findViewById(R.id.rv_favorites);
        layoutEmpty  = view.findViewById(R.id.layout_favorites_empty);

        favoriteRepository = new FavoriteRepository();

        adapter = new FavoriteStoreAdapter();
        adapter.setListener(new FavoriteStoreAdapter.OnFavoriteActionListener() {
            @Override
            public void onStoreClick(Favorite favorite) {
                // Mở StoreDetailActivity với storeId đã lưu
                Intent intent = new Intent(requireContext(), StoreDetailActivity.class);
                intent.putExtra("storeId",   favorite.getItemId());
                intent.putExtra("storeName", favorite.getName());
                intent.putExtra("storeImage", favorite.getImageUrl());
                startActivity(intent);
            }

            @Override
            public void onRemoveClick(Favorite favorite) {
                showRemoveConfirmDialog(favorite);
            }
        });

        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFavorites.setAdapter(adapter);

        loadFavorites();
    }

    private void loadFavorites() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";
        if (uid.isEmpty()) {
            showEmpty(true);
            return;
        }

        favoriteRepository.getStoreFavorites(uid).observe(getViewLifecycleOwner(), favorites -> {
            List<Favorite> list = favorites != null ? favorites : new ArrayList<>();
            adapter.setItems(list);
            tvCount.setText(list.size() + " quán");
            showEmpty(list.isEmpty());
        });
    }

    private void showRemoveConfirmDialog(Favorite favorite) {
        if (favorite == null || !isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Bỏ yêu thích?")
                .setMessage("Bỏ \"" + favorite.getName() + "\" khỏi danh sách yêu thích?")
                .setPositiveButton("Bỏ yêu thích", (dialog, which) -> {
                    favoriteRepository.removeFavorite(favorite.getId())
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(requireContext(),
                                            "Đã bỏ yêu thích",
                                            Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(),
                                            "Lỗi: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEmpty(boolean empty) {
        rvFavorites.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}

