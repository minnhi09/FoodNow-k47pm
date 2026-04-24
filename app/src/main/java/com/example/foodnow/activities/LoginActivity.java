package com.example.foodnow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodnow.MainActivity;
import com.example.foodnow.R;
import com.example.foodnow.viewmodels.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etEmail     = findViewById(R.id.et_email);
        etPassword  = findViewById(R.id.et_password);
        btnLogin    = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        TextView tvGoRegister = findViewById(R.id.tv_go_register);

        // Nếu đã đăng nhập trước đó → vẫn cần fetch role để route đúng màn hình
        com.google.firebase.auth.FirebaseUser currentUser = authViewModel.getUserLiveData().getValue();
        if (currentUser != null) {
            authViewModel.loadUserProfile(currentUser.getUid());
            // observer bên dưới sẽ xử lý routing
        }

        // Observe profile đầy đủ (có role) — được set sau khi login thành công
        authViewModel.getUserProfileLiveData().observe(this, userProfile -> {
            if (userProfile == null) return;
            // Route sang màn hình đúng theo role
            if ("store_owner".equals(userProfile.getRole())) {
                goToStoreOwner(userProfile.getStoreId());
            } else {
                // customer hoặc admin đều vào MainActivity
                goToMain();
            }
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getLoadingLiveData().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!isLoading);
        });

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            authViewModel.login(email, password);
        });

        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToStoreOwner(String storeId) {
        Intent intent = new Intent(this, StoreOwnerActivity.class);
        intent.putExtra("storeId", storeId != null ? storeId : "");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

