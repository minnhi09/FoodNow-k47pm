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
import com.example.foodnow.models.RecommendedFood;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RecommendedFoodAdapter extends RecyclerView.Adapter<RecommendedFoodAdapter.ViewHolder> {

    public interface OnAddClickListener {
        void onAddClick(RecommendedFood food);
    }

    private final Context context;
    private final List<RecommendedFood> recommendedFoods;
    private final OnAddClickListener addClickListener;
    private final NumberFormat currencyFormatter;

    public RecommendedFoodAdapter(Context context, List<RecommendedFood> recommendedFoods, OnAddClickListener addClickListener) {
        this.context = context;
        this.recommendedFoods = recommendedFoods;
        this.addClickListener = addClickListener;
        this.currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommended_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendedFood food = recommendedFoods.get(position);
        holder.tvFoodName.setText(food.getName());
        holder.tvStoreName.setText(food.getStoreName());
        holder.tvPrice.setText(currencyFormatter.format(food.getPrice()) + "đ");
        holder.tvRating.setText(String.format(Locale.US, "%.1f", food.getRating()));
        holder.tvPopularBadge.setVisibility(food.isPopular() ? View.VISIBLE : View.GONE);

        Glide.with(context)
                .load(food.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imgFood);

        holder.tvAddButton.setOnClickListener(v -> {
            if (addClickListener != null) {
                addClickListener.onAddClick(food);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recommendedFoods.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvPopularBadge;
        TextView tvRating;
        TextView tvFoodName;
        TextView tvStoreName;
        TextView tvPrice;
        TextView tvAddButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.img_food);
            tvPopularBadge = itemView.findViewById(R.id.tv_food_popular_badge);
            tvRating = itemView.findViewById(R.id.tv_food_rating);
            tvFoodName = itemView.findViewById(R.id.tv_food_name);
            tvStoreName = itemView.findViewById(R.id.tv_food_store_name);
            tvPrice = itemView.findViewById(R.id.tv_food_price);
            tvAddButton = itemView.findViewById(R.id.tv_add_food);
        }
    }
}
