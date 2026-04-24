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

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.CheckoutViewHolder> {

    private final Context context;
    private final List<CartItem> items;
    private final NumberFormat nf;

    public CheckoutItemAdapter(Context context, List<CartItem> items) {
        this.context = context;
        this.items = items;
        this.nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout_food, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem item = items.get(position);
        
        holder.tvName.setText(item.getTitle());
        holder.tvStore.setText(item.getStoreName());
        holder.tvQuantity.setText("x" + item.getQuantity());
        holder.tvPrice.setText(nf.format(item.getTotalPrice()) + "đ");

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgFood);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgFood;
        final TextView tvName, tvStore, tvQuantity, tvPrice;

        CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.img_food);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStore = itemView.findViewById(R.id.tv_store);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }
    }
}
