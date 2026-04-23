package com.example.foodnow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.activities.StoreOwnerActivity;
import com.example.foodnow.viewmodels.StoreOwnerViewModel;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Tab "Quán của tôi" — hiển thị thông tin quán và công tắc mở/đóng cửa.
 */
public class StoreOwnerDashboardFragment extends Fragment {

    private StoreOwnerViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_store_owner_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy storeId từ Activity cha
        String storeId = ((StoreOwnerActivity) requireActivity()).getStoreId();

        // Dùng chung ViewModel với các Fragment khác trong StoreOwnerActivity
        viewModel = new ViewModelProvider(requireActivity()).get(StoreOwnerViewModel.class);

        ImageView imgCover       = view.findViewById(R.id.img_store_cover);
        TextView tvName          = view.findViewById(R.id.tv_store_name);
        TextView tvAddress       = view.findViewById(R.id.tv_store_address);
        TextView tvPhone         = view.findViewById(R.id.tv_store_phone);
        TextView tvRating        = view.findViewById(R.id.tv_store_rating);
        TextView tvDeliveryTime  = view.findViewById(R.id.tv_delivery_time);
        TextView tvDeliveryFee   = view.findViewById(R.id.tv_delivery_fee);
        TextView tvDescription   = view.findViewById(R.id.tv_store_description);
        Switch switchOpen        = view.findViewById(R.id.switch_open);

        NumberFormat currFmt = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Quan sát dữ liệu quán real-time từ Firestore
        viewModel.getStore(storeId).observe(getViewLifecycleOwner(), store -> {
            if (store == null) return;

            tvName.setText(store.getName());
            tvAddress.setText("📍 " + store.getAddress());
            tvPhone.setText("📞 " + store.getPhone());
            tvRating.setText(String.format(Locale.US, "%.1f ⭐", store.getRating()));
            tvDeliveryTime.setText(store.getDeliveryTime());
            tvDeliveryFee.setText(currFmt.format(store.getDeliveryFee()) + "đ");
            tvDescription.setText(store.getDescription());

            // Đặt trạng thái công tắc mở/đóng
            switchOpen.setChecked(store.isOpen());

            // Load ảnh quán
            Glide.with(this)
                    .load(store.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imgCover);

            // Khi chủ nhà hàng bật/tắt công tắc → cập nhật Firestore
            switchOpen.setOnCheckedChangeListener((btn, isChecked) -> {
                store.setOpen(isChecked);
                // Cập nhật trực tiếp qua repo (không cần method riêng trong VM)
                new com.example.foodnow.repositories.StoreRepository()
                        .updateStore(storeId, store);
            });
        });
    }
}

