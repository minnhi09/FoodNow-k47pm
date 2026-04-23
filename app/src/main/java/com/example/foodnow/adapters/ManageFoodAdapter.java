package com.example.foodnow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.models.Food;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho màn quản lý thực đơn (ManageFoodsFragment).
 * Hỗ trợ: toggle ẩn/hiện món, nút Sửa / Xóa.
 */
public class ManageFoodAdapter extends RecyclerView.Adapter<ManageFoodAdapter.ViewHolder> {

    public interface OnEditListener     { void onEdit(Food food); }
    public interface OnDeleteListener   { void onDelete(Food food); }
    public interface OnToggleListener   { void onToggle(Food food, boolean isAvailable); }

    private final Context           context;
    private final List<Food>        allFoods = new ArrayList<>();   // full list
    private final List<Food>        filtered = new ArrayList<>();    // displayed list
    private final OnEditListener    editListener;
    private final OnDeleteListener  deleteListener;
    private final OnToggleListener  toggleListener;
    private final NumberFormat      currFmt  = NumberFormat.getInstance(new Locale("vi", "VN"));

    private String searchQuery    = "";
    private String categoryFilter = null;   // null = all

    public ManageFoodAdapter(Context context, List<Food> foods,
                             OnEditListener editListener,
                             OnDeleteListener deleteListener,
                             OnToggleListener toggleListener) {
        this.context        = context;
        this.editListener   = editListener;
        this.deleteListener = deleteListener;
        this.toggleListener = toggleListener;
        setFoods(foods);
    }

    public void setFoods(List<Food> foods) {
        allFoods.clear();
        if (foods != null) allFoods.addAll(foods);
        applyFilter();
    }

    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query.toLowerCase().trim();
        applyFilter();
    }

    public void setCategoryFilter(String categoryId) {
        categoryFilter = categoryId;
        applyFilter();
    }

    private void applyFilter() {
        filtered.clear();
        for (Food f : allFoods) {
            boolean matchSearch   = searchQuery.isEmpty()
                    || f.getTitle().toLowerCase().contains(searchQuery);
            boolean matchCategory = categoryFilter == null
                    || categoryFilter.equals(f.getCategoryId());
            if (matchSearch && matchCategory) filtered.add(f);
        }
        notifyDataSetChanged();
    }

    public int getFilteredCount() { return filtered.size(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_owner_manage_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food food = filtered.get(position);

        holder.tvTitle.setText(food.getTitle());
        holder.tvPrice.setText(currFmt.format(food.getPrice()) + "đ");

        // Badge trạng thái
        if (food.isAvailable()) {
            holder.tvAvailable.setText("Đang bán");
            holder.tvAvailable.setBackgroundColor(0xFF4CAF50);
        } else {
            holder.tvAvailable.setText("Tạm ẩn");
            holder.tvAvailable.setBackgroundColor(0xFF9E9E9E);
        }

        // Toggle switch — tắt listener trước để tránh trigger khi bind
        holder.switchAvailable.setOnCheckedChangeListener(null);
        holder.switchAvailable.setChecked(food.isAvailable());
        holder.switchAvailable.setOnCheckedChangeListener((btn, checked) -> {
            food.setAvailable(checked);
            holder.tvAvailable.setText(checked ? "Đang bán" : "Tạm ẩn");
            holder.tvAvailable.setBackgroundColor(checked ? 0xFF4CAF50 : 0xFF9E9E9E);
            if (toggleListener != null) toggleListener.onToggle(food, checked);
        });

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
    public int getItemCount() { return filtered.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView    imgFood;
        TextView     tvTitle, tvPrice, tvAvailable;
        Switch       switchAvailable;
        MaterialButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood         = itemView.findViewById(R.id.img_food);
            tvTitle         = itemView.findViewById(R.id.tv_food_title);
            tvPrice         = itemView.findViewById(R.id.tv_food_price);
            tvAvailable     = itemView.findViewById(R.id.tv_available);
            switchAvailable = itemView.findViewById(R.id.switch_available);
            btnEdit         = itemView.findViewById(R.id.btn_edit);
            btnDelete       = itemView.findViewById(R.id.btn_delete);
        }
    }
}

