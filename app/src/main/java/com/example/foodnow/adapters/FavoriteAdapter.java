package com.example.foodnow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.models.CartItem;
import com.example.foodnow.models.Favorite;
import com.example.foodnow.utils.CartManager;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    public interface OnFavoriteListener {
        void onRemove(Favorite favorite);
        void onClick(Favorite favorite);
    }

    private final Context context;
    private final List<Favorite> favorites = new ArrayList<>();
    private final OnFavoriteListener listener;

    public FavoriteAdapter(Context context, OnFavoriteListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<Favorite> newFavorites) {
        favorites.clear();
        if (newFavorites != null) {
            favorites.addAll(newFavorites);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Favorite favorite = favorites.get(position);

        holder.tvName.setText(favorite.getName());
        
        boolean isStore = "store".equalsIgnoreCase(favorite.getType());
        
        if (isStore) {
            holder.layoutStoreExtra.setVisibility(View.VISIBLE);
            holder.layoutFoodAction.setVisibility(View.GONE);
            holder.tvCategory.setText("Cửa hàng");
        } else {
            holder.layoutStoreExtra.setVisibility(View.GONE);
            holder.layoutFoodAction.setVisibility(View.VISIBLE);
            holder.tvPrice.setText("55.000đ"); // Giá mẫu
            holder.tvCategory.setText("Món ăn");
        }

        // Cập nhật rating (giả định 4.8 nếu trống)
        holder.tvRating.setText("4.8");

        Glide.with(context)
                .load(favorite.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgFavorite);

        holder.btnHeart.setOnClickListener(v -> {
            if (listener != null) listener.onRemove(favorite);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(favorite);
        });

        // Chức năng thêm vào giỏ hàng nhanh cho Món ăn
        holder.btnAddToCart.setOnClickListener(v -> {
            if (!isStore) {
                CartItem item = new CartItem(
                        favorite.getItemId(),
                        favorite.getName(),
                        55000, 
                        1,
                        favorite.getImageUrl(),
                        "unknown_store",
                        "Cửa hàng"
                );
                CartManager.getInstance().addItem(item);
                Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgFavorite, btnHeart;
        final TextView btnAddToCart;
        final TextView tvName, tvCategory, tvRating, tvPrice;
        final View layoutStoreExtra, layoutFoodAction;

        FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFavorite = itemView.findViewById(R.id.img_favorite);
            btnHeart = itemView.findViewById(R.id.btn_favorite_heart);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart_quick);
            tvName = itemView.findViewById(R.id.tv_favorite_name);
            tvCategory = itemView.findViewById(R.id.tv_favorite_category);
            tvRating = itemView.findViewById(R.id.tv_favorite_rating_value);
            tvPrice = itemView.findViewById(R.id.tv_favorite_price);
            layoutStoreExtra = itemView.findViewById(R.id.layout_store_extra);
            layoutFoodAction = itemView.findViewById(R.id.layout_food_price_action);
        }
    }
}
