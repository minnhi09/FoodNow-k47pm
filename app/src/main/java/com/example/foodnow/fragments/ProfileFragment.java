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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.foodnow.R;
import com.example.foodnow.activities.EditProfileActivity;
import com.example.foodnow.activities.ImageAdminActivity;
import com.example.foodnow.activities.LoginActivity;
import com.example.foodnow.viewmodels.AuthViewModel;
import com.example.foodnow.viewmodels.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private AuthViewModel authViewModel;

    private ImageView imgAvatar;
    private TextView tvUserName, tvUserEmail, tvPointsSubtitle;
    private View rowUserHeader, rowPersonalInfo, rowAddress, rowPayment;
    private View rowVouchers, rowPoints;
    private View rowNotifications, rowAppSettings, rowHelp;
    private View rowAdmin, dividerAdmin;
    private View rowLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgAvatar        = view.findViewById(R.id.img_avatar);
        tvUserName       = view.findViewById(R.id.tv_user_name);
        tvUserEmail      = view.findViewById(R.id.tv_user_email);
        tvPointsSubtitle = view.findViewById(R.id.tv_points_subtitle);
        rowUserHeader    = view.findViewById(R.id.row_user_header);
        rowPersonalInfo  = view.findViewById(R.id.row_personal_info);
        rowAddress       = view.findViewById(R.id.row_address);
        rowPayment       = view.findViewById(R.id.row_payment);
        rowVouchers      = view.findViewById(R.id.row_vouchers);
        rowPoints        = view.findViewById(R.id.row_points);
        rowNotifications = view.findViewById(R.id.row_notifications);
        rowAppSettings   = view.findViewById(R.id.row_app_settings);
        rowHelp          = view.findViewById(R.id.row_help);
        rowAdmin         = view.findViewById(R.id.row_admin);
        dividerAdmin     = view.findViewById(R.id.divider_admin);
        rowLogout        = view.findViewById(R.id.row_logout);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        authViewModel    = new ViewModelProvider(this).get(AuthViewModel.class);

        // Quan sát thông tin user
        profileViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            tvUserName.setText(user.getName() != null ? user.getName() : "");
            tvUserEmail.setText(user.getPhone() != null ? user.getPhone() : "");
            tvPointsSubtitle.setText("0 điểm");

            // Chỉ admin mới thấy mục Quản trị ảnh
            boolean isAdmin = "admin".equals(user.getRole());
            rowAdmin.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            dividerAdmin.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

            if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                Glide.with(requireContext())
                        .load(user.getImageUrl())
                        .circleCrop()
                        .placeholder(R.drawable.bg_profile_avatar)
                        .into(imgAvatar);
            }
        });

        // Mở màn hình chỉnh sửa thông tin
        View.OnClickListener openEditProfile = v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivity(intent);
        };
        rowUserHeader.setOnClickListener(openEditProfile);
        rowPersonalInfo.setOnClickListener(openEditProfile);

        // Các mục chưa có màn hình → thông báo
        View.OnClickListener comingSoon = v ->
                Toast.makeText(requireContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        rowAddress.setOnClickListener(comingSoon);
        rowPayment.setOnClickListener(comingSoon);
        rowVouchers.setOnClickListener(comingSoon);
        rowPoints.setOnClickListener(comingSoon);
        rowNotifications.setOnClickListener(comingSoon);
        rowAppSettings.setOnClickListener(comingSoon);
        rowHelp.setOnClickListener(comingSoon);

        // Admin: mở màn hình quản trị ảnh
        rowAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ImageAdminActivity.class);
            startActivity(intent);
        });

        // Đăng xuất
        rowLogout.setOnClickListener(v -> {
            authViewModel.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}

