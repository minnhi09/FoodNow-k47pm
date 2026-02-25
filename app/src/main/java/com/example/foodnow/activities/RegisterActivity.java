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

public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private TextInputEditText etName, etPhone, etEmail, etPassword;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Ánh xạ view
        etName      = findViewById(R.id.et_name);
        etPhone     = findViewById(R.id.et_phone);
        etEmail     = findViewById(R.id.et_email);
        etPassword  = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);
        TextView tvGoLogin = findViewById(R.id.tv_go_login);

        // Quan sát trạng thái
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
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

        // Xử lý bấm nút đăng ký
        btnRegister.setOnClickListener(v -> {
            String name     = etName.getText().toString().trim();
            String phone    = etPhone.getText().toString().trim();
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            authViewModel.register(email, password, name, phone);
        });

        // Quay lại đăng nhập
        tvGoLogin.setOnClickListener(v -> finish());
    }
}
