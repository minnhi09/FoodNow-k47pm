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
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho màn quản lý thực đơn (ManageFoodsFragment).
 * Mỗi item hiển thị ảnh, tên, giá và 2 nút: Sửa / Xóa.
 */
public class ManageFoodAdapter extends RecyclerView.Adapter<ManageFoodAdapter.ViewHolder> {

    public interface OnEditListener  { void onEdit(Food food); }
    public interface OnDeleteListener { void onDelete(Food food); }

    private final Context context;
    private final List<Food> foods;
    private final OnEditListener editListener;
    private final OnDeleteListener deleteListener;
    private final NumberFormat currFmt = NumberFormat.getInstance(new Locale("vi", "VN"));

    public ManageFoodAdapter(Context context, List<Food> foods,
                             OnEditListener editListener,
                             OnDeleteListener deleteListener) {
        this.context        = context;
        this.foods          = foods;
        this.editListener   = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_manage_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food food = foods.get(position);

        holder.tvTitle.setText(food.getTitle());
        holder.tvPrice.setText(currFmt.format(food.getPrice()) + "đ");

        // Badge màu theo trạng thái
        if (food.isAvailable()) {
            holder.tvAvailable.setText("Đang bán");
            holder.tvAvailable.setBackgroundColor(0xFF4CAF50);
        } else {
            holder.tvAvailable.setText("Tạm ẩn");
            holder.tvAvailable.setBackgroundColor(0xFF9E9E9E);
        }

        Glide.with(context)
                .load(food.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imgFood);

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(food);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(food);
        });
    }

    @Override
    public int getItemCount() { return foods.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvTitle, tvPrice, tvAvailable;
        MaterialButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood     = itemView.findViewById(R.id.img_food);
            tvTitle     = itemView.findViewById(R.id.tv_food_title);
            tvPrice     = itemView.findViewById(R.id.tv_food_price);
            tvAvailable = itemView.findViewById(R.id.tv_available);
            btnEdit     = itemView.findViewById(R.id.btn_edit);
            btnDelete   = itemView.findViewById(R.id.btn_delete);
        }
    }
}
