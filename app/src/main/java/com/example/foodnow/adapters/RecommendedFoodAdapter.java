package com.example.foodnow.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.activities.FoodDetailActivity;
import com.example.foodnow.models.Food;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecommendedFoodAdapter extends RecyclerView.Adapter<RecommendedFoodAdapter.ViewHolder> {

    private final Context context;
    private final List<Food> foods = new ArrayList<>();
    private Map<String, String> storeNames = new HashMap<>();
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

    public RecommendedFoodAdapter(Context context) {
        this.context = context;
    }

    /** Cập nhật dữ liệu — gọi sau khi cả foods lẫn storeNames đã sẵn sàng */
    public void setData(List<Food> newFoods, Map<String, String> newStoreNames) {
        foods.clear();
        if (newFoods != null) foods.addAll(newFoods);
        storeNames = newStoreNames != null ? newStoreNames : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recommended_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Food food = foods.get(position);

        h.tvFoodName.setText(food.getTitle() != null ? food.getTitle() : "");
        h.tvStoreName.setText(storeNames.containsKey(food.getStoreId())
                ? storeNames.get(food.getStoreId())
                : "");
        h.tvPrice.setText(currencyFormatter.format(food.getPrice()) + "đ");
        h.tvRating.setText(String.format(Locale.US, "%.1f", food.getRating()));
        h.tvPopularBadge.setVisibility(food.getRating() >= 4.7f ? View.VISIBLE : View.GONE);

        Glide.with(context)
                .load(food.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(h.imgFood);

        // Click "+" hoặc click vào card → mở FoodDetailActivity
        View.OnClickListener openDetail = v -> {
            Intent intent = new Intent(context, FoodDetailActivity.class);
            intent.putExtra("foodId",    food.getId());
            intent.putExtra("foodTitle", food.getTitle());
            intent.putExtra("foodImage", food.getImageUrl());
            intent.putExtra("foodPrice", food.getPrice());
            intent.putExtra("foodRating", food.getRating());
            intent.putExtra("foodDescription", food.getDescription());
            intent.putExtra("storeId",   food.getStoreId());
            intent.putExtra("storeName", storeNames.getOrDefault(food.getStoreId(), ""));
            context.startActivity(intent);
        };
        h.tvAddButton.setOnClickListener(openDetail);
        h.itemView.setOnClickListener(openDetail);
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgFood;
        final TextView tvPopularBadge, tvRating, tvFoodName, tvStoreName, tvPrice, tvAddButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood        = itemView.findViewById(R.id.img_food);
            tvPopularBadge = itemView.findViewById(R.id.tv_food_popular_badge);
            tvRating       = itemView.findViewById(R.id.tv_food_rating);
            tvFoodName     = itemView.findViewById(R.id.tv_food_name);
            tvStoreName    = itemView.findViewById(R.id.tv_food_store_name);
            tvPrice        = itemView.findViewById(R.id.tv_food_price);
            tvAddButton    = itemView.findViewById(R.id.tv_add_food);
        }
    }
}
