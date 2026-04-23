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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Adapter hiển thị danh sách đơn hàng trong tab Đơn hàng (StoreOwnerActivity). */
public class StoreOrderAdapter extends RecyclerView.Adapter<StoreOrderAdapter.ViewHolder> {

    public interface OnOrderActionListener {
        void onPrimaryAction(Order order);   // Xác nhận / Sẵn sàng / Hoàn thành
        void onRejectAction(Order order);    // Từ chối / Hủy
    }

    private final List<Order>          orders   = new ArrayList<>();
    private final OnOrderActionListener listener;
    private final NumberFormat         currFmt;
    private final SimpleDateFormat     timeFmt  = new SimpleDateFormat("HH:mm", new Locale("vi"));

    public StoreOrderAdapter(OnOrderActionListener listener) {
        this.listener = listener;
        this.currFmt  = NumberFormat.getInstance(new Locale("vi", "VN"));
    }

    public void setOrders(List<Order> newOrders) {
        orders.clear();
        if (newOrders != null) orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_owner_store_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Order order = orders.get(position);

        // Order ID (rút ngắn)
        String shortId = "#" + order.getId().substring(0, Math.min(8, order.getId().length()));
        h.tvOrderId.setText(shortId);

        // Status badge — Vietnamese text + drawable shape
        h.tvStatus.setText(toVietnameseStatus(order.getStatus()));
        h.tvStatus.setBackgroundResource(statusBadgeDrawable(order.getStatus()));
        h.tvStatus.setTextColor(0xFFFFFFFF);

        // Time (với prefix đồng hồ)
        if (order.getCreatedAt() != null) {
            h.tvTime.setText("⏱ " + timeFmt.format(order.getCreatedAt().toDate()));
        }

        // Customer (không có emoji)
        String customerDisplay = order.getCustomerName() != null && !order.getCustomerName().isEmpty()
                ? order.getCustomerName()
                : "Khách hàng";
        h.tvCustomer.setText(customerDisplay);

        // Items summary
        h.tvItems.setText(order.getItemsSummary());

        // Total
        h.tvTotal.setText(currFmt.format((long) order.getTotal()) + "đ");

        // Action buttons
        bindActionButtons(h, order);
    }

    private void bindActionButtons(ViewHolder h, Order order) {
        String status = order.getStatus();

        switch (status) {
            case Order.STATUS_NEW:
                h.btnReject.setVisibility(View.VISIBLE);
                h.btnPrimary.setVisibility(View.VISIBLE);
                h.btnPrimary.setText("Xác nhận");
                h.btnPrimary.setOnClickListener(v -> listener.onPrimaryAction(order));
                h.btnReject.setOnClickListener(v -> listener.onRejectAction(order));
                break;

            case Order.STATUS_PROCESSING:
                h.btnReject.setVisibility(View.GONE);
                h.btnPrimary.setVisibility(View.VISIBLE);
                h.btnPrimary.setText("Sẵn sàng giao");
                h.btnPrimary.setOnClickListener(v -> listener.onPrimaryAction(order));
                break;

            case Order.STATUS_READY:
                h.btnReject.setVisibility(View.GONE);
                h.btnPrimary.setVisibility(View.VISIBLE);
                h.btnPrimary.setText("Đã giao");
                h.btnPrimary.setOnClickListener(v -> listener.onPrimaryAction(order));
                break;

            default:
                h.btnReject.setVisibility(View.GONE);
                h.btnPrimary.setVisibility(View.GONE);
                break;
        }
    }

    /** Drawable bo góc cho badge theo trạng thái đơn hàng */
    private int statusBadgeDrawable(String status) {
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

    @Override
    public int getItemCount() { return orders.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvOrderId, tvStatus, tvTime, tvCustomer, tvItems, tvTotal;
        final TextView btnPrimary, btnReject;

        ViewHolder(View v) {
            super(v);
            tvOrderId  = v.findViewById(R.id.tv_order_id);
            tvStatus   = v.findViewById(R.id.tv_order_status);
            tvTime     = v.findViewById(R.id.tv_order_time);
            tvCustomer = v.findViewById(R.id.tv_customer_name);
            tvItems    = v.findViewById(R.id.tv_order_items);
            tvTotal    = v.findViewById(R.id.tv_order_total);
            btnPrimary = v.findViewById(R.id.btn_primary_action);
            btnReject  = v.findViewById(R.id.btn_reject);
        }
    }
}
