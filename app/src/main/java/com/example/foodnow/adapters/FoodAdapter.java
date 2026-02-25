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
import com.example.foodnow.models.Food;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private Context context;
    private List<Food> foodList;

    public interface OnFoodClickListener {
        void onFoodClick(Food food);
    }
    private OnFoodClickListener listener;

    public FoodAdapter(Context context, List<Food> foodList, OnFoodClickListener listener) {
        this.context  = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);

        holder.tvTitle.setText(food.getTitle());
        holder.tvDescription.setText(food.getDescription());
        holder.tvRating.setText("⭐ " + food.getRating());

        // Hiển thị giá theo định dạng tiền Việt
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(nf.format(food.getPrice()) + "đ");

        // Load ảnh bằng Glide
        if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(food.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.imgFood);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onFoodClick(food);
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvTitle, tvDescription, tvPrice, tvRating;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood       = itemView.findViewById(R.id.img_food);
            tvTitle       = itemView.findViewById(R.id.tv_food_title);
            tvDescription = itemView.findViewById(R.id.tv_food_description);
            tvPrice       = itemView.findViewById(R.id.tv_food_price);
            tvRating      = itemView.findViewById(R.id.tv_food_rating);
        }
    }
}
