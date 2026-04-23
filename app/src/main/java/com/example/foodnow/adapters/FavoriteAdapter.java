package com.example.foodnow.adapters;

import android.content.Context;
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

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    public interface OnFavoriteRemoveListener {
        void onRemove(Favorite fav);
    }

    private final Context context;
    private final List<Favorite> favoriteList;
    private final OnFavoriteRemoveListener listener;

    public FavoriteAdapter(Context context, List<Favorite> favoriteList, OnFavoriteRemoveListener listener) {
        this.context = context;
        this.favoriteList = favoriteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Favorite fav = favoriteList.get(position);

        Glide.with(context)
                .load(fav.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgFavoriteImage);

        holder.tvFavoriteName.setText(fav.getName() != null ? fav.getName() : "");

        String typeText = "";
        if ("store".equals(fav.getType())) {
            typeText = "Quán ăn";
        } else if ("food".equals(fav.getType())) {
            typeText = "Món ăn";
        }
        holder.tvFavoriteType.setText(typeText);

        holder.btnRemoveFavorite.setOnClickListener(v -> listener.onRemove(fav));
    }

    @Override
    public int getItemCount() {
        return favoriteList != null ? favoriteList.size() : 0;
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgFavoriteImage;
        final TextView tvFavoriteName;
        final TextView tvFavoriteType;
        final ImageView btnRemoveFavorite;

        FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFavoriteImage = itemView.findViewById(R.id.img_favorite_image);
            tvFavoriteName = itemView.findViewById(R.id.tv_favorite_name);
            tvFavoriteType = itemView.findViewById(R.id.tv_favorite_type);
            btnRemoveFavorite = itemView.findViewById(R.id.btn_remove_favorite);
        }
    }
}
