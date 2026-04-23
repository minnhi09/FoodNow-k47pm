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

    private final Context context;
    private final List<Order> orderList;
    private final NumberFormat numberFormat;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderStoreName.setText(order.getStoreName() != null ? order.getStoreName() : "");
        holder.tvOrderStatus.setText(order.getStatus() != null ? order.getStatus() : "");

        int itemCount = 0;
        if (order.getItems() != null) {
            itemCount = order.getItems().size();
        }
        holder.tvOrderItemCount.setText(itemCount + " món");

        holder.tvOrderTotalPrice.setText(numberFormat.format(order.getTotal()) + "đ");
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        final TextView tvOrderStoreName;
        final TextView tvOrderStatus;
        final TextView tvOrderItemCount;
        final TextView tvOrderTotalPrice;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderStoreName = itemView.findViewById(R.id.tv_order_store_name);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderItemCount = itemView.findViewById(R.id.tv_order_item_count);
            tvOrderTotalPrice = itemView.findViewById(R.id.tv_order_total_price);
        }
    }
}
