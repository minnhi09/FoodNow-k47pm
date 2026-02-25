package com.example.foodnow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.models.Order;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context   = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvStore.setText(order.getStoreName());
        holder.tvStatus.setText(order.getStatus());

        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        holder.tvItemsCount.setText(itemCount + " món");

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvTotal.setText(nf.format(order.getTotal()) + "đ");
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvStore, tvStatus, tvItemsCount, tvTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStore      = itemView.findViewById(R.id.tv_order_store);
            tvStatus     = itemView.findViewById(R.id.tv_order_status);
            tvItemsCount = itemView.findViewById(R.id.tv_order_items_count);
            tvTotal      = itemView.findViewById(R.id.tv_order_total);
        }
    }
}
