package com.example.foodnow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.foodnow.activities.LoginActivity;
import com.example.foodnow.R;
import com.example.foodnow.activities.EditProfileActivity;
import com.example.foodnow.activities.StoreOwnerActivity;
import com.example.foodnow.repositories.UserRepository;
import com.example.foodnow.viewmodels.StoreOwnerViewModel;
import com.google.firebase.auth.FirebaseAuth;

/** Tab "Cài đặt" trong StoreOwnerActivity. */
public class OwnerSettingsFragment extends Fragment {

    private StoreOwnerViewModel viewModel;
    private UserRepository      userRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String storeId = ((StoreOwnerActivity) requireActivity()).getStoreId();
        viewModel  = new ViewModelProvider(requireActivity()).get(StoreOwnerViewModel.class);
        userRepo   = new UserRepository();

        // ── Cover card views ──
        ImageView imgCover         = view.findViewById(R.id.img_store_cover_settings);
        ImageView imgAvatar        = view.findViewById(R.id.img_user_avatar_settings);
        TextView  tvStoreName      = view.findViewById(R.id.tv_settings_store_name);
        TextView  tvRating         = view.findViewById(R.id.tv_settings_rating);
        ImageView btnEditStore     = view.findViewById(R.id.btn_edit_store);
        TextView  btnChangeCover   = view.findViewById(R.id.btn_change_cover);

        // ── Info section views ──
        TextView tvStoreNameDetail = view.findViewById(R.id.tv_store_name_detail);
        TextView tvStoreAddress    = view.findViewById(R.id.tv_settings_store_address);
        TextView tvStorePhone      = view.findViewById(R.id.tv_settings_store_phone);
        TextView tvStoreDesc       = view.findViewById(R.id.tv_settings_store_description);

        // ── Info row click targets ──
        View rowStoreName    = view.findViewById(R.id.row_store_name);
        View rowStoreAddress = view.findViewById(R.id.row_store_address);
        View rowStorePhone   = view.findViewById(R.id.row_store_phone);
        View rowStoreDesc    = view.findViewById(R.id.row_store_desc);

        // ── Payment & Delivery row click targets ──
        View rowDelivery = view.findViewById(R.id.row_delivery_settings);
        View rowVouchers = view.findViewById(R.id.row_vouchers);
        View rowPayment  = view.findViewById(R.id.row_payment_methods);

        // ── Rating & Support row click targets ──
        View rowReviews = view.findViewById(R.id.row_reviews);
        View rowPolicy  = view.findViewById(R.id.row_policy);
        View rowHelp    = view.findViewById(R.id.row_help);

        // ── Action buttons ──
        TextView btnSwitchCustomer = view.findViewById(R.id.btn_switch_to_customer_settings);
        TextView btnLogout         = view.findViewById(R.id.btn_logout_settings);

        // ── Load store data ──
        viewModel.getStore(storeId).observe(getViewLifecycleOwner(), store -> {
            if (store == null) return;

            String name = store.getName() != null ? store.getName() : "—";
            tvStoreName.setText(name);
            tvStoreNameDetail.setText(name);
            tvStoreAddress.setText(store.getAddress() != null ? store.getAddress() : "—");
            tvStorePhone.setText(store.getPhone() != null ? store.getPhone() : "—");
            tvStoreDesc.setText(store.getDescription() != null ? store.getDescription() : "Chưa có mô tả");

            double rating = store.getRating();
            tvRating.setText(rating > 0 ? String.format("%.1f", rating) : "Mới");

            String coverUrl = store.getImageUrl();
            if (coverUrl != null && !coverUrl.isEmpty()) {
                Glide.with(this).load(coverUrl).centerCrop().into(imgCover);
            }
        });

        // ── Load user avatar ──
        userRepo.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            String avatarUrl = user.getImageUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this).load(avatarUrl).circleCrop().into(imgAvatar);
            }
        });

        // ── Store info row clicks → coming soon ──
        View.OnClickListener comingSoon = v ->
                Toast.makeText(requireContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();

        btnEditStore.setOnClickListener(comingSoon);
        btnChangeCover.setOnClickListener(comingSoon);
        rowStoreName.setOnClickListener(comingSoon);
        rowStoreAddress.setOnClickListener(comingSoon);
        rowStorePhone.setOnClickListener(comingSoon);
        rowStoreDesc.setOnClickListener(comingSoon);
        rowDelivery.setOnClickListener(comingSoon);
        rowVouchers.setOnClickListener(comingSoon);
        rowPayment.setOnClickListener(comingSoon);
        rowReviews.setOnClickListener(comingSoon);
        rowPolicy.setOnClickListener(comingSoon);
        rowHelp.setOnClickListener(v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));

        // ── Switch to customer mode ──
        btnSwitchCustomer.setOnClickListener(v ->
                ((StoreOwnerActivity) requireActivity()).switchToCustomer());

        // ── Logout ──
        btnLogout.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", null)
                .show());
    }
}


