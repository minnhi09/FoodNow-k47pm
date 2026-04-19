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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {

    // ① Khai báo biến
    private final Context context;
    private final List<Store> storeList;
    private final OnStoreClickListener listener;
    private final NumberFormat currencyFormatter;

    // ② Interface để xử lý sự kiện click
    public interface OnStoreClickListener {
        void onStoreClick(Store store);
    }

    // ③ Constructor
    public StoreAdapter(Context context, List<Store> storeList, OnStoreClickListener listener) {
        this.context = context;
        this.storeList = storeList;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
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
        holder.tvStoreName.setText(safeText(store.getName(), "Quán ăn"));
        holder.tvStoreAddress.setText(safeText(store.getAddress(), "Chưa có địa chỉ"));
        holder.tvStoreTime.setText(getDeliveryTimeText(store.getDeliveryTime()));
        holder.tvStoreDeliveryFee.setText(getDeliveryFeeText(store.getDeliveryFee()));
        holder.tvStoreRating.setText(getRatingText(store.getRating()));

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
        TextView tvStoreName, tvStoreAddress, tvStoreTime, tvStoreDeliveryFee, tvStoreRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStore        = itemView.findViewById(R.id.img_store);
            tvStoreName     = itemView.findViewById(R.id.tv_store_name);
            tvStoreAddress  = itemView.findViewById(R.id.tv_store_address);
            tvStoreTime     = itemView.findViewById(R.id.tv_store_time);
            tvStoreDeliveryFee = itemView.findViewById(R.id.tv_store_delivery_fee);
            tvStoreRating   = itemView.findViewById(R.id.tv_store_rating);
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

    private String getDeliveryFeeText(long deliveryFee) {
        if (deliveryFee <= 0) {
            return "Phí giao hàng: Miễn phí";
        }
        return "Phí giao hàng: " + currencyFormatter.format(deliveryFee) + "đ";
    }

    private String getRatingText(float rating) {
        if (rating <= 0f) {
            return "0.0";
        }
        return String.format(Locale.US, "%.1f", rating);
    }
}
