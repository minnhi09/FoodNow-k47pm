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

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {

    // ① Khai báo biến
    private Context context;
    private List<Store> storeList;
    private OnStoreClickListener listener;

    // ② Interface để xử lý sự kiện click
    public interface OnStoreClickListener {
        void onStoreClick(Store store);
    }

    // ③ Constructor
    public StoreAdapter(Context context, List<Store> storeList, OnStoreClickListener listener) {
        this.context = context;
        this.storeList = storeList;
        this.listener = listener;
    }

    // ④ Inflate layout item_store.xml
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_store, parent, false);
        return new ViewHolder(view);
    }

    // ⑤ Bind dữ liệu vào từng item
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Store store = storeList.get(position);

        // Gán text
        holder.tvStoreName.setText(store.getName());
        holder.tvStoreCategory.setText("Việt Nam"); // tạm thời fix cứng
        holder.tvStoreTime.setText(store.getDeliveryTime());
        holder.tvStoreDistance.setText("1.2 km");   // tạm thời fix cứng
        holder.tvStoreRating.setText(String.valueOf(store.getRating()));

        // Load ảnh bằng Glide
        Glide.with(context)
                .load(store.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imgStore);

        // Xử lý click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStoreClick(store);
            }
        });
    }

    // ⑥ Trả về số lượng item
    @Override
    public int getItemCount() {
        return storeList.size();
    }

    // ⑦ ViewHolder — giữ tham chiếu đến các view trong item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgStore;
        TextView tvStoreName, tvStoreCategory, tvStoreTime, tvStoreDistance, tvStoreRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStore        = itemView.findViewById(R.id.img_store);
            tvStoreName     = itemView.findViewById(R.id.tv_store_name);
            tvStoreCategory = itemView.findViewById(R.id.tv_store_category);
            tvStoreTime     = itemView.findViewById(R.id.tv_store_time);
            tvStoreDistance = itemView.findViewById(R.id.tv_store_distance);
            tvStoreRating   = itemView.findViewById(R.id.tv_store_rating);
        }
    }
}