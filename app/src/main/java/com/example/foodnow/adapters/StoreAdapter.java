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
import com.example.foodnow.models.Store;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    // ① Dữ liệu và ngữ cảnh
    private Context context;
    private List<Store> storeList;

    // ② Interface xử lý sự kiện click
    public interface OnStoreClickListener {
        void onStoreClick(Store store);
    }
    private OnStoreClickListener listener;

    // ③ Constructor — nhận dữ liệu từ bên ngoài truyền vào
    public StoreAdapter(Context context, List<Store> storeList,
                        OnStoreClickListener listener) {
        this.context   = context;
        this.storeList = storeList;
        this.listener  = listener;
    }

    // ─────────────────────────────────────────────
    // BƯỚC 1: Tạo "khuôn" cho mỗi dòng
    // ─────────────────────────────────────────────
    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item_store.xml → tạo View cho 1 dòng
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(itemView);
    }

    // ─────────────────────────────────────────────
    // BƯỚC 2: Đổ dữ liệu vào "khuôn" theo vị trí
    // ─────────────────────────────────────────────
    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        // Lấy quán tại vị trí hiện tại
        Store store = storeList.get(position);

        // Đổ dữ liệu vào TextView
        holder.tvName.setText(store.getName());
        holder.tvCategory.setText(store.getDescription());
        holder.tvRating.setText("⭐ " + store.getRating());
        holder.tvTime.setText(store.getDeliveryTime());

        // Load ảnh bằng Glide
        if (store.getImageUrl() != null && !store.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(store.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.imgStore);
        }

        // Xử lý sự kiện click vào 1 dòng
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStoreClick(store);
            }
        });
    }

    // ─────────────────────────────────────────────
    // BƯỚC 3: Tổng số dòng
    // ─────────────────────────────────────────────
    @Override
    public int getItemCount() {
        return storeList.size();
    }

    // ─────────────────────────────────────────────
    // ViewHolder — "khuôn" giữ tham chiếu các View
    // ─────────────────────────────────────────────
    public static class StoreViewHolder extends RecyclerView.ViewHolder {
        ImageView imgStore;
        TextView tvName, tvCategory, tvRating, tvTime;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            // Tìm các View trong item_store.xml theo id
            imgStore   = itemView.findViewById(R.id.img_store);
            tvName     = itemView.findViewById(R.id.tv_store_name);
            tvCategory = itemView.findViewById(R.id.tv_store_category);
            tvRating   = itemView.findViewById(R.id.tv_store_rating);
            tvTime     = itemView.findViewById(R.id.tv_delivery_time);
        }
    }
}