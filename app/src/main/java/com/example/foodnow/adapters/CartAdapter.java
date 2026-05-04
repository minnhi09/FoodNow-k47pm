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
import com.example.foodnow.models.CartItem;
import com.example.foodnow.utils.CartManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemChangeListener {
        void onQuantityChanged();
    }

    private final Context context;
    private final List<CartItem> cartList;
    private final OnCartItemChangeListener listener;
    private final NumberFormat numberFormat;

    public CartAdapter(Context context, List<CartItem> cartList, OnCartItemChangeListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
        this.numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartList.get(position);

        holder.tvCartFoodName.setText(cartItem.getTitle());
        holder.tvStoreName.setText(cartItem.getStoreName());
        holder.tvCartFoodPrice.setText(numberFormat.format(cartItem.getPrice()) + "đ");
        holder.tvCartQuantity.setText(String.valueOf(cartItem.getQuantity()));

        Glide.with(context)
                .load(cartItem.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgCartFood);

        holder.btnPlus.setOnClickListener(v -> {
            // Tăng số lượng trực tiếp trong CartItem hiện tại
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            notifyItemChanged(holder.getBindingAdapterPosition());
            listener.onQuantityChanged();
        });

        holder.btnMinus.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                notifyItemChanged(pos);
            } else {
                CartManager.getInstance().removeItem(cartItem.getFoodId());
                notifyItemRemoved(pos);
            }
            listener.onQuantityChanged();
        });

        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            CartManager.getInstance().removeItemFully(cartItem.getFoodId());
            notifyItemRemoved(pos);
            listener.onQuantityChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartList != null ? cartList.size() : 0;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgCartFood, btnRemove;
        final TextView tvCartFoodName, tvStoreName;
        final TextView tvCartFoodPrice;
        final TextView btnMinus;
        final TextView tvCartQuantity;
        final TextView btnPlus;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCartFood = itemView.findViewById(R.id.img_cart_food);
            btnRemove = itemView.findViewById(R.id.btn_remove);
            tvCartFoodName = itemView.findViewById(R.id.tv_cart_food_name);
            tvStoreName = itemView.findViewById(R.id.tv_cart_store_name);
            tvCartFoodPrice = itemView.findViewById(R.id.tv_cart_food_price);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            tvCartQuantity = itemView.findViewById(R.id.tv_cart_quantity);
            btnPlus = itemView.findViewById(R.id.btn_plus);
        }
    }
}
