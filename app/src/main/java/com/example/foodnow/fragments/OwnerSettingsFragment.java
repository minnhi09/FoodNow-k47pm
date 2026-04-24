package com.example.foodnow.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.foodnow.activities.EditStoreActivity;
import com.example.foodnow.repositories.StoreRepository;
import com.example.foodnow.repositories.UserRepository;
import com.example.foodnow.utils.CloudinaryHelper;
import com.example.foodnow.viewmodels.StoreOwnerViewModel;
import com.google.firebase.auth.FirebaseAuth;

/** Tab "Cài đặt" trong StoreOwnerActivity. */
public class OwnerSettingsFragment extends Fragment {

    private StoreOwnerViewModel viewModel;
    private UserRepository      userRepo;

    private String       currentStoreId;
    private ImageView    imgCoverField;
    private ProgressBar  progressCoverField;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                uploadCoverImage(uri);
            });

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

        currentStoreId = ((StoreOwnerActivity) requireActivity()).getStoreId();
        viewModel  = new ViewModelProvider(requireActivity()).get(StoreOwnerViewModel.class);
        userRepo   = new UserRepository();

        imgCoverField              = view.findViewById(R.id.img_store_cover_settings);
        progressCoverField         = view.findViewById(R.id.progress_cover_upload);
        ImageView imgAvatar        = view.findViewById(R.id.img_user_avatar_settings);
        TextView  tvStoreName      = view.findViewById(R.id.tv_settings_store_name);
        TextView  tvRating         = view.findViewById(R.id.tv_settings_rating);
        ImageView btnEditStore     = view.findViewById(R.id.btn_edit_store);
        TextView  btnChangeCover   = view.findViewById(R.id.btn_change_cover);

        TextView tvStoreNameDetail = view.findViewById(R.id.tv_store_name_detail);
        TextView tvStoreAddress    = view.findViewById(R.id.tv_settings_store_address);
        TextView tvStorePhone      = view.findViewById(R.id.tv_settings_store_phone);
        TextView tvStoreDesc       = view.findViewById(R.id.tv_settings_store_description);

        // Các row thông tin cửa hàng chỉ đọc — không cần bind vào Java
        View rowDelivery = view.findViewById(R.id.row_delivery_settings);
        View rowVouchers = view.findViewById(R.id.row_vouchers);
        View rowPayment  = view.findViewById(R.id.row_payment_methods);

        View rowReviews = view.findViewById(R.id.row_reviews);
        View rowPolicy  = view.findViewById(R.id.row_policy);
        View rowHelp    = view.findViewById(R.id.row_help);

        TextView btnLogout         = view.findViewById(R.id.btn_logout_settings);

        viewModel.getStore(currentStoreId).observe(getViewLifecycleOwner(), store -> {
            if (store == null) return;
            String name = store.getName() != null ? store.getName() : "-";
            tvStoreName.setText(name);
            tvStoreNameDetail.setText(name);
            tvStoreAddress.setText(store.getAddress() != null ? store.getAddress() : "-");
            tvStorePhone.setText(store.getPhone() != null ? store.getPhone() : "-");
            tvStoreDesc.setText(store.getDescription() != null ? store.getDescription() : "Chưa có mô tả");
            double rating = store.getRating();
            tvRating.setText(rating > 0 ? String.format("%.1f", rating) : "Mới");
            String coverUrl = store.getImageUrl();
            if (coverUrl != null && !coverUrl.isEmpty()) {
                Glide.with(this).load(coverUrl).centerCrop().into(imgCoverField);
            }
        });

        userRepo.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            String avatarUrl = user.getImageUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this).load(avatarUrl).circleCrop().into(imgAvatar);
            }
        });

        btnChangeCover.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Icon bút → mở màn hình chỉnh sửa thông tin cửa hàng
        btnEditStore.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditStoreActivity.class);
            intent.putExtra(EditStoreActivity.EXTRA_STORE_ID, currentStoreId);
            startActivity(intent);
        });

        View.OnClickListener comingSoon = v ->
                Toast.makeText(requireContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();

        // Các row thông tin cửa hàng là chỉ đọc (chỉnh sửa qua icon bút)
        rowDelivery.setOnClickListener(comingSoon);
        rowVouchers.setOnClickListener(comingSoon);
        rowPayment.setOnClickListener(comingSoon);
        rowReviews.setOnClickListener(comingSoon);
        rowPolicy.setOnClickListener(comingSoon);
        rowHelp.setOnClickListener(v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));

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

    private void uploadCoverImage(Uri imageUri) {
        progressCoverField.setVisibility(View.VISIBLE);
        CloudinaryHelper.uploadImage(requireContext(), imageUri,
                CloudinaryHelper.FOLDER_STORES,
                new CloudinaryHelper.OnUploadCallback() {
                    @Override public void onStart() {}

                    @Override
                    public void onSuccess(String secureUrl) {
                        new StoreRepository()
                                .updateStoreImageUrl(currentStoreId, secureUrl)
                                .addOnSuccessListener(unused -> {
                                    if (!isAdded()) return;
                                    requireActivity().runOnUiThread(() -> {
                                        progressCoverField.setVisibility(View.GONE);
                                        Glide.with(OwnerSettingsFragment.this)
                                                .load(secureUrl).centerCrop().into(imgCoverField);
                                        Toast.makeText(requireContext(),
                                                "Đổi ảnh bìa thành công!", Toast.LENGTH_SHORT).show();
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    if (!isAdded()) return;
                                    requireActivity().runOnUiThread(() -> {
                                        progressCoverField.setVisibility(View.GONE);
                                        Toast.makeText(requireContext(),
                                                "Lỗi lưu ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                                });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            progressCoverField.setVisibility(View.GONE);
                            Toast.makeText(requireContext(),
                                    "Lỗi upload: " + errorMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }
}