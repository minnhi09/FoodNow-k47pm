package com.example.foodnow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodnow.MainActivity;
import com.example.foodnow.R;
import com.example.foodnow.viewmodels.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    // Trường tài khoản chung
    private TextInputEditText etName, etPhone, etEmail, etPassword;
    // Trường thông tin cửa hàng
    private TextInputEditText etStoreName, etStoreAddress, etStorePhone,
            etDeliveryTime, etDeliveryFee;

    private RadioGroup rgRole;
    private LinearLayout sectionStoreInfo;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Ánh xạ view
        etName          = findViewById(R.id.et_name);
        etPhone         = findViewById(R.id.et_phone);
        etEmail         = findViewById(R.id.et_email);
        etPassword      = findViewById(R.id.et_password);
        etStoreName     = findViewById(R.id.et_store_name);
        etStoreAddress  = findViewById(R.id.et_store_address);
        etStorePhone    = findViewById(R.id.et_store_phone);
        etDeliveryTime  = findViewById(R.id.et_delivery_time);
        etDeliveryFee   = findViewById(R.id.et_delivery_fee);
        rgRole          = findViewById(R.id.rg_role);
        sectionStoreInfo = findViewById(R.id.section_store_info);
        btnRegister     = findViewById(R.id.btn_register);
        progressBar     = findViewById(R.id.progress_bar);
        TextView tvGoLogin = findViewById(R.id.tv_go_login);

        // Hiện/ẩn phần thông tin cửa hàng theo loại tài khoản
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isStoreOwner = checkedId == R.id.rb_store_owner;
            sectionStoreInfo.setVisibility(isStoreOwner ? View.VISIBLE : View.GONE);
        });

        // Đăng ký khách hàng → chuyển sang MainActivity
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // Đăng ký chủ cửa hàng → chuyển sang StoreOwnerActivity với storeId
        authViewModel.getStoreOwnerRegisteredLiveData().observe(this, storeId -> {
            if (storeId != null && !storeId.isEmpty()) {
                Intent intent = new Intent(this, StoreOwnerActivity.class);
                intent.putExtra("storeId", storeId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getLoadingLiveData().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnRegister.setEnabled(!isLoading);
        });

        btnRegister.setOnClickListener(v -> handleRegister());

        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String name     = etName.getText().toString().trim();
        String phone    = etPhone.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isStoreOwner = rgRole.getCheckedRadioButtonId() == R.id.rb_store_owner;

        if (isStoreOwner) {
            String storeName    = etStoreName.getText().toString().trim();
            String storeAddress = etStoreAddress.getText().toString().trim();
            String storePhone   = etStorePhone.getText().toString().trim();
            String deliveryTime = etDeliveryTime.getText().toString().trim();
            String feeStr       = etDeliveryFee.getText().toString().trim();

            if (storeName.isEmpty() || storeAddress.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và địa chỉ cửa hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            long deliveryFee = 0;
            try {
                if (!feeStr.isEmpty()) deliveryFee = Long.parseLong(feeStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Phí giao hàng phải là số", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.registerStoreOwner(email, password, name, phone,
                    storeName, storeAddress, storePhone, deliveryTime, deliveryFee);
        } else {
            authViewModel.register(email, password, name, phone);
        }
    }
}
