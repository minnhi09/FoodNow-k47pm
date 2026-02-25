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

    private Context context;
    private List<Favorite> favoriteList;

    public interface OnFavoriteRemoveListener {
        void onRemove(Favorite favorite);
    }
    private OnFavoriteRemoveListener listener;

    public FavoriteAdapter(Context context, List<Favorite> favoriteList,
                           OnFavoriteRemoveListener listener) {
        this.context      = context;
        this.favoriteList = favoriteList;
        this.listener     = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Favorite fav = favoriteList.get(position);

        holder.tvName.setText(fav.getName());
        holder.tvType.setText("store".equals(fav.getType()) ? "Quán ăn" : "Món ăn");

        if (fav.getImageUrl() != null && !fav.getImageUrl().isEmpty()) {
            Glide.with(context).load(fav.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher).into(holder.imgFavorite);
        }

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemove(fav);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFavorite, btnRemove;
        TextView tvName, tvType;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFavorite = itemView.findViewById(R.id.img_favorite);
            tvName      = itemView.findViewById(R.id.tv_favorite_name);
            tvType      = itemView.findViewById(R.id.tv_favorite_type);
            btnRemove   = itemView.findViewById(R.id.btn_remove_favorite);
        }
    }
}
