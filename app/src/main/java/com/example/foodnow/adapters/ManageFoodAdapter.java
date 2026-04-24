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
        holder.tvDescription.setText(food.getDescription() != null ? food.getDescription() : "");
        holder.tvSold.setText("Đã bán: " + food.getSoldCount());
        holder.tvCategory.setText(food.getCategoryId() != null ? food.getCategoryId() : "");

        // Hot badge nếu soldCount >= 20
        holder.tvHotBadge.setVisibility(food.getSoldCount() >= 20 ? View.VISIBLE : View.GONE);

        // Toggle trạng thái
        updateAvailableButton(holder.tvAvailable, food.isAvailable());
        holder.tvAvailable.setOnClickListener(v -> {
            boolean newVal = !food.isAvailable();
            food.setAvailable(newVal);
            updateAvailableButton(holder.tvAvailable, newVal);
            if (toggleListener != null) toggleListener.onToggle(food, newVal);
        });

        Glide.with(context)
                .load(food.getImageUrl())
                .placeholder(R.drawable.bg_image_rounded)
                .into(holder.imgFood);

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(food);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(food);
        });
    }

    private void updateAvailableButton(TextView tv, boolean available) {
        if (available) {
            tv.setText("👁 Đang bán");
            tv.setBackgroundResource(R.drawable.bg_food_action_green);
            tv.setTextColor(0xFF388E3C);
        } else {
            tv.setText("🚫 Ẩn món");
            tv.setBackgroundResource(R.drawable.bg_food_action_grey);
            tv.setTextColor(0xFF757575);
        }
    }

    @Override
    public int getItemCount() { return filtered.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView  imgFood;
        TextView   tvTitle, tvPrice, tvDescription, tvSold, tvCategory, tvAvailable, tvHotBadge;
        TextView   btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood        = itemView.findViewById(R.id.img_food);
            tvTitle        = itemView.findViewById(R.id.tv_food_title);
            tvPrice        = itemView.findViewById(R.id.tv_food_price);
            tvDescription  = itemView.findViewById(R.id.tv_food_description);
            tvSold         = itemView.findViewById(R.id.tv_food_sold);
            tvCategory     = itemView.findViewById(R.id.tv_food_category);
            tvAvailable    = itemView.findViewById(R.id.tv_available);
            tvHotBadge     = itemView.findViewById(R.id.tv_hot_badge);
            btnEdit        = itemView.findViewById(R.id.btn_edit);
            btnDelete      = itemView.findViewById(R.id.btn_delete);
        }
    }
}

