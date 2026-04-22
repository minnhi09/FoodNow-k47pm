package com.example.foodnow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.models.Category;

import java.util.List;
import java.util.Locale;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<Category> categoryList;
    private final OnCategoryClickListener listener;

    // ID của danh mục đang được chọn, "" = không chọn gì (Tất cả)
    private String selectedCategoryId = "";

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }
    public CategoryAdapter(Context context, List<Category> categoryList,
                           OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    /** Cập nhật danh mục đang chọn và refresh giao diện */
    public void setSelectedCategory(String categoryId) {
        this.selectedCategoryId = categoryId == null ? "" : categoryId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvName.setText(category.getName());
        holder.imgCategory.setImageResource(getCategoryIconRes(category.getName()));

        // Kiểm tra chip này có đang được chọn không
        boolean isSelected = category.getId() != null
                && category.getId().equals(selectedCategoryId);

        // Đổi màu nền icon: cam đậm khi chọn, cam nhạt khi không chọn
        holder.iconContainer.setBackgroundResource(
                isSelected ? R.drawable.bg_home_category_icon_selected
                           : R.drawable.bg_home_category_icon);

        // Đổi màu icon: trắng khi chọn, cam khi không chọn
        holder.imgCategory.setColorFilter(context.getColor(
                isSelected ? R.color.white : R.color.home_primary_orange));

        // Đổi màu chữ: cam khi chọn, đen khi không chọn
        holder.tvName.setTextColor(context.getColor(
                isSelected ? R.color.home_primary_orange : R.color.home_text_primary));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconContainer;
        ImageView imgCategory;
        TextView tvName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.frame_category_icon);
            imgCategory = itemView.findViewById(R.id.img_category);
            tvName      = itemView.findViewById(R.id.tv_category_name);
        }
    }

    private int getCategoryIconRes(String name) {
        if (name == null) {
            return android.R.drawable.ic_menu_sort_by_size;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        if (normalized.contains("tất cả") || normalized.contains("tat ca")) {
            return android.R.drawable.ic_menu_sort_by_size;
        }
        if (normalized.contains("phở") || normalized.contains("pho")) {
            return android.R.drawable.ic_menu_crop;
        }
        if (normalized.contains("pizza")) {
            return android.R.drawable.ic_menu_compass;
        }
        if (normalized.contains("tráng") || normalized.contains("trang")) {
            return android.R.drawable.ic_menu_gallery;
        }
        return android.R.drawable.ic_menu_agenda;
    }
}
