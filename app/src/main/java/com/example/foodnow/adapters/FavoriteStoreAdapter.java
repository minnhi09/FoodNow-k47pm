package com.example.foodnow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.models.Favorite;

import java.util.ArrayList;
import java.util.List;

public class FavoriteStoreAdapter extends RecyclerView.Adapter<FavoriteStoreAdapter.ViewHolder> {

    public interface OnFavoriteActionListener {
        void onStoreClick(Favorite favorite);
        void onRemoveClick(Favorite favorite);
    }

    private final List<Favorite> items = new ArrayList<>();
    private OnFavoriteActionListener listener;

    public void setListener(OnFavoriteActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Favorite> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_store, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Favorite fav = items.get(position);

        h.tvName.setText(fav.getName() != null ? fav.getName() : "");
        h.tvType.setText("Quán ăn yêu thích");

        if (fav.getImageUrl() != null && !fav.getImageUrl().isEmpty()) {
            Glide.with(h.ivImage.getContext())
                    .load(fav.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(h.ivImage);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onStoreClick(fav);
        });
        h.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemoveClick(fav);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivImage, btnRemove;
        final TextView tvName, tvType;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage   = itemView.findViewById(R.id.iv_fav_store_image);
            tvName    = itemView.findViewById(R.id.tv_fav_store_name);
            tvType    = itemView.findViewById(R.id.tv_fav_store_type);
            btnRemove = itemView.findViewById(R.id.btn_remove_favorite);
        }
    }
}
