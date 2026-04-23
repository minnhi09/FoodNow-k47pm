package com.example.foodnow.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.foodnow.R;
import com.example.foodnow.fragments.ManageFoodsFragment;
import com.example.foodnow.fragments.OwnerSettingsFragment;
import com.example.foodnow.fragments.StatsFragment;
import com.example.foodnow.fragments.StoreOrdersFragment;
import com.example.foodnow.fragments.StoreOwnerDashboardFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class StoreOwnerActivity extends AppCompatActivity {

    /** storeId được truyền vào từ LoginActivity sau khi xác định role = store_owner */
    private String storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_owner);

        storeId = getIntent().getStringExtra("storeId");
        if (storeId == null) storeId = "";

        // Nếu tài khoản chưa được liên kết với cửa hàng → báo lỗi và đăng xuất
        if (storeId.isEmpty()) {
            showStoreNotLinkedError();
            return;
        }

        BottomNavigationView bottomNav = findViewById(R.id.owner_bottom_nav);

        // Tab mặc định khi mở app
        loadFragment(new StoreOwnerDashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();
            if (id == R.id.nav_owner_dashboard) {
                fragment = new StoreOwnerDashboardFragment();
            } else if (id == R.id.nav_owner_orders) {
                fragment = new StoreOrdersFragment();
            } else if (id == R.id.nav_owner_foods) {
                fragment = new ManageFoodsFragment();
            } else if (id == R.id.nav_owner_stats) {
                fragment = new StatsFragment();
            } else {
                // nav_owner_settings
                fragment = new OwnerSettingsFragment();
            }
            loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.owner_fragment_container, fragment)
                .commit();
    }

    /** Hiện dialog khi storeId rỗng — tài khoản chưa được liên kết với cửa hàng */
    private void showStoreNotLinkedError() {
        new AlertDialog.Builder(this)
                .setTitle("Lỗi tài khoản")
                .setMessage("Tài khoản của bạn chưa được liên kết với cửa hàng. Vui lòng liên hệ quản trị viên.")
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    /** Chuyển sang màn hình khách hàng (MainActivity). */
    public void switchToCustomer() {
        Intent intent = new Intent(this, com.example.foodnow.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /** Các Fragment con có thể gọi để lấy storeId */
    public String getStoreId() {
        return storeId;
    }
}

