package com.example.foodnow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.models.Food;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    // ① Interface xử lý sự kiện thêm vào giỏ
    public interface OnAddToCartListener {
        void onAddToCart(Food food);
    }

    private final Context context;
    private final List<Food> foodList;
    private final OnAddToCartListener listener;
    private final NumberFormat currencyFormatter;

    // ② Constructor
    public FoodAdapter(Context context, List<Food> foodList, OnAddToCartListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
    }

    // ③ Inflate layout item_food.xml
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    // ④ Bind dữ liệu vào từng item
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food food = foodList.get(position);

        holder.tvTitle.setText(safeText(food.getTitle(), "Món ăn"));
        holder.tvDescription.setText(safeText(food.getDescription(), ""));
        holder.tvPrice.setText(currencyFormatter.format(food.getPrice()) + "đ");
        holder.tvRating.setText(String.format(Locale.US, "%.1f", food.getRating()));

        // Load ảnh bằng Glide
        Glide.with(context)
                .load(food.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imgFood);

        // Xử lý click nút thêm vào giỏ
        holder.btnAddCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(food);
            }
        });
    }

    // ⑤ Trả về số lượng item
    @Override
    public int getItemCount() {
        return foodList.size();
    }

    // ⑥ ViewHolder — giữ tham chiếu đến các view trong item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvTitle, tvDescription, tvPrice, tvRating, btnAddCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.img_food);
            tvTitle = itemView.findViewById(R.id.tv_food_title);
            tvDescription = itemView.findViewById(R.id.tv_food_description);
            tvPrice = itemView.findViewById(R.id.tv_food_price);
            tvRating = itemView.findViewById(R.id.tv_food_rating);
            btnAddCart = itemView.findViewById(R.id.btn_add_cart);
        }
    }

    private String safeText(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) return defaultValue;
        return value;
    }
}
