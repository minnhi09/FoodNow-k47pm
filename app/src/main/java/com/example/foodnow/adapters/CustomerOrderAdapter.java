package com.example.foodnow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.models.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerOrderAdapter extends RecyclerView.Adapter<CustomerOrderAdapter.ViewHolder> {
    private final List<Order> orders = new ArrayList<>();
    private final NumberFormat currFmt = NumberFormat.getInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("dd/MM HH:mm", new Locale("vi", "VN"));

    public void setOrders(List<Order> newOrders) {
        orders.clear();
        if (newOrders != null) orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Order order = orders.get(position);

        String orderId = order.getId() != null ? order.getId() : "";
        h.tvOrderId.setText("#" + orderId.substring(0, Math.min(8, orderId.length())).toUpperCase());
        h.tvStatus.setText(toVietnameseStatus(order.getStatus()));
        h.tvStatus.setBackgroundResource(statusBadge(order.getStatus()));
        h.tvStore.setText("Quán: " + (order.getStoreName() != null ? order.getStoreName() : "—"));
        h.tvItems.setText(order.getItemsSummary());
        h.tvTotal.setText(currFmt.format((long) order.getTotal()) + "đ");

        if (order.getCreatedAt() != null) {
            h.tvTime.setText("⏱ " + timeFmt.format(order.getCreatedAt().toDate()));
        } else {
            h.tvTime.setText("⏱ --:--");
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private int statusBadge(String status) {
        if (status == null) return R.drawable.bg_status_badge;
        switch (status) {
            case Order.STATUS_NEW:        return R.drawable.bg_status_badge_blue;
            case Order.STATUS_PROCESSING: return R.drawable.bg_status_badge_orange;
            case Order.STATUS_READY:      return R.drawable.bg_status_badge_green;
            case Order.STATUS_DELIVERING: return R.drawable.bg_status_badge_purple;
            case Order.STATUS_DONE:       return R.drawable.bg_status_badge_green;
            case Order.STATUS_CANCELLED:  return R.drawable.bg_status_badge;
            default:                      return R.drawable.bg_status_badge;
        }
    }

    private String toVietnameseStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case Order.STATUS_NEW:        return "Đơn mới";
            case Order.STATUS_PROCESSING: return "Đang làm";
            case Order.STATUS_READY:      return "Sẵn sàng";
            case Order.STATUS_DELIVERING: return "Đang giao";
            case Order.STATUS_DONE:       return "Hoàn thành";
            case Order.STATUS_CANCELLED:  return "Đã hủy";
            default:                      return status;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvOrderId, tvStatus, tvStore, tvItems, tvTime, tvTotal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_customer_order_id);
            tvStatus = itemView.findViewById(R.id.tv_customer_order_status);
            tvStore = itemView.findViewById(R.id.tv_customer_order_store);
            tvItems = itemView.findViewById(R.id.tv_customer_order_items);
            tvTime = itemView.findViewById(R.id.tv_customer_order_time);
            tvTotal = itemView.findViewById(R.id.tv_customer_order_total);
        }
    }
}
