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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;

    public interface OnCartItemChangeListener {
        void onQuantityChanged();
    }
    private OnCartItemChangeListener listener;

    public CartAdapter(Context context, List<CartItem> cartItems,
                       OnCartItemChangeListener listener) {
        this.context   = context;
        this.cartItems = cartItems;
        this.listener  = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(nf.format(item.getPrice()) + "đ");

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context).load(item.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher).into(holder.imgFood);
        }

        // Nút +
        holder.btnPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
            if (listener != null) listener.onQuantityChanged();
        });

        // Nút −
        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(position);
            } else {
                cartItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartItems.size());
            }
            if (listener != null) listener.onQuantityChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvTitle, tvPrice, tvQuantity, btnPlus, btnMinus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood    = itemView.findViewById(R.id.img_cart_food);
            tvTitle    = itemView.findViewById(R.id.tv_cart_title);
            tvPrice    = itemView.findViewById(R.id.tv_cart_price);
            tvQuantity = itemView.findViewById(R.id.tv_cart_quantity);
            btnPlus    = itemView.findViewById(R.id.btn_plus);
            btnMinus   = itemView.findViewById(R.id.btn_minus);
        }
    }
}
