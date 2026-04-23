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
import com.example.foodnow.models.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final NumberFormat numberFormat;
    private final SimpleDateFormat dateFormat;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm", new Locale("vi", "VN"));
    }

    public void submitList(List<Order> newOrders) {
        orderList.clear();
        if (newOrders != null) {
            orderList.addAll(newOrders);
        }
        notifyDataSetChanged();
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

        // Hiển thị mã đơn hàng (giả định dùng 8 ký tự cuối của ID)
        String orderId = order.getId();
        if (orderId != null && orderId.length() > 8) {
            holder.tvOrderId.setText("#GF" + orderId.substring(orderId.length() - 8).toUpperCase());
        } else {
            holder.tvOrderId.setText("#GF-ORDER");
        }

        holder.tvOrderStatus.setText(order.getStatus() != null ? order.getStatus() : "Đang xử lý");
        
        if (order.getCreatedAt() != null) {
            holder.tvOrderTime.setText(dateFormat.format(order.getCreatedAt().toDate()));
        }

        holder.tvOrderStoreName.setText(order.getStoreName() != null ? order.getStoreName() : "Cửa hàng");

        int itemCount = (order.getItems() != null) ? order.getItems().size() : 0;
        String countAndPrice = itemCount + " món • " + numberFormat.format(order.getTotal()) + "đ";
        holder.tvOrderItemCountTotal.setText(countAndPrice);

        // Hiển thị tóm tắt món ăn
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            StringBuilder summary = new StringBuilder();
            for (int i = 0; i < order.getItems().size(); i++) {
                summary.append(order.getItems().get(i).getTitle());
                if (i < order.getItems().size() - 1) summary.append(", ");
                if (summary.length() > 40) break; // Giới hạn chiều dài
            }
            holder.tvOrderItemsSummary.setText(summary.toString());
            
            // Lấy ảnh của món đầu tiên làm đại diện quán
            Glide.with(context)
                    .load(order.getItems().get(0).getImageUrl())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.imgOrderStore);
        }

        // Tùy chỉnh nút dựa trên trạng thái
        String status = order.getStatus();
        if ("Hoàn thành".equalsIgnoreCase(status) || "Đã hủy".equalsIgnoreCase(status)) {
            holder.btnActionSecondary.setText("Đánh giá");
            holder.btnActionPrimary.setText("Đặt lại");
            holder.layoutDeliveryInfo.setVisibility(View.GONE);
        } else {
            holder.btnActionSecondary.setText("Theo dõi");
            holder.btnActionPrimary.setText("Chi tiết");
            holder.layoutDeliveryInfo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        final TextView tvOrderId, tvOrderStatus, tvOrderTime;
        final ImageView imgOrderStore;
        final TextView tvOrderStoreName, tvOrderItemCountTotal, tvOrderItemsSummary;
        final TextView btnActionSecondary, btnActionPrimary;
        final View layoutDeliveryInfo;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderTime = itemView.findViewById(R.id.tv_order_time);
            imgOrderStore = itemView.findViewById(R.id.img_order_store);
            tvOrderStoreName = itemView.findViewById(R.id.tv_order_store_name);
            tvOrderItemCountTotal = itemView.findViewById(R.id.tv_order_item_count_total);
            tvOrderItemsSummary = itemView.findViewById(R.id.tv_order_items_summary);
            btnActionSecondary = itemView.findViewById(R.id.btn_action_secondary);
            btnActionPrimary = itemView.findViewById(R.id.btn_action_primary);
            layoutDeliveryInfo = itemView.findViewById(R.id.layout_delivery_info);
        }
    }
}
