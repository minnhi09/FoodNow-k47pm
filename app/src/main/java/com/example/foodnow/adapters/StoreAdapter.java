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
import java.util.Locale;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {

    // ① Khai báo biến
    private final Context context;
    private final List<Store> storeList;
    private final OnStoreClickListener listener;

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

        // Gán text theo style Figma
        holder.tvStoreName.setText(safeText(store.getName(), "Quán ăn"));
        holder.tvStoreCountry.setText(getCountryText(store.getAddress()));
        holder.tvStoreTime.setText(getDeliveryTimeText(store.getDeliveryTime()));
        holder.tvStoreDistance.setText(getDistanceText(position));
        holder.tvStoreRating.setText(getRatingText(store.getRating()));
        holder.tvStoreBadge.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

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
        TextView tvStoreName, tvStoreCountry, tvStoreTime, tvStoreDistance, tvStoreRating, tvStoreBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStore = itemView.findViewById(R.id.img_store);
            tvStoreName = itemView.findViewById(R.id.tv_store_name);
            tvStoreCountry = itemView.findViewById(R.id.tv_store_country);
            tvStoreTime = itemView.findViewById(R.id.tv_store_time);
            tvStoreDistance = itemView.findViewById(R.id.tv_store_distance);
            tvStoreRating = itemView.findViewById(R.id.tv_store_rating);
            tvStoreBadge = itemView.findViewById(R.id.tv_store_badge);
        }
    }

    private String safeText(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    private String getDeliveryTimeText(String deliveryTime) {
        return safeText(deliveryTime, "Đang cập nhật");
    }

    private String getCountryText(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "Việt Nam";
        }
        return address;
    }

    private String getDistanceText(int position) {
        String[] distances = {"1.2 km", "0.8 km", "0.5 km", "1.4 km", "1.0 km"};
        return distances[position % distances.length];
    }

    private String getRatingText(float rating) {
        if (rating <= 0f) {
            return "0.0";
        }
        return String.format(Locale.US, "%.1f", rating);
    }
}
