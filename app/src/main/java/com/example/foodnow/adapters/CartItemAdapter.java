package com.example.foodnow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.models.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {
    public interface OnCartItemActionListener {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
        void onRemove(CartItem item);
    }

    private final List<CartItem> items = new ArrayList<>();
    private final OnCartItemActionListener listener;
    private final NumberFormat currFmt = NumberFormat.getInstance(new Locale("vi", "VN"));

    public CartItemAdapter(OnCartItemActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CartItem> cartItems) {
        items.clear();
        if (cartItems != null) items.addAll(cartItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_food, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CartItem item = items.get(position);
        h.tvName.setText(item.getTitle());
        h.tvPrice.setText(currFmt.format(item.getPrice()) + "đ");
        h.tvQty.setText(String.valueOf(item.getQuantity()));

        Glide.with(h.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(h.imgFood);

        h.btnIncrease.setOnClickListener(v -> {
            if (listener != null) listener.onIncrease(item);
        });
        h.btnDecrease.setOnClickListener(v -> {
            if (listener != null) listener.onDecrease(item);
        });
        h.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemove(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgFood;
        final TextView tvName, tvPrice, tvQty, btnIncrease, btnDecrease, btnRemove;

        ViewHolder(@NonNull View v) {
            super(v);
            imgFood = v.findViewById(R.id.img_cart_food);
            tvName = v.findViewById(R.id.tv_cart_food_name);
            tvPrice = v.findViewById(R.id.tv_cart_food_price);
            tvQty = v.findViewById(R.id.tv_cart_quantity);
            btnIncrease = v.findViewById(R.id.btn_cart_increase);
            btnDecrease = v.findViewById(R.id.btn_cart_decrease);
            btnRemove = v.findViewById(R.id.btn_cart_remove);
        }
    }
}
