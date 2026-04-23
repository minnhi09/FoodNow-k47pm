package com.example.foodnow.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.foodnow.R;
import com.example.foodnow.fragments.ManageFoodsFragment;
import com.example.foodnow.fragments.ProfileFragment;
import com.example.foodnow.fragments.StoreOrdersFragment;
import com.example.foodnow.fragments.StoreOwnerDashboardFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StoreOwnerActivity extends AppCompatActivity {

    /** storeId được truyền vào từ LoginActivity sau khi xác định role = store_owner */
    private String storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_owner);

        storeId = getIntent().getStringExtra("storeId");
        if (storeId == null) storeId = "";

        BottomNavigationView bottomNav = findViewById(R.id.owner_bottom_nav);

        // Tab mặc định khi mở app
        loadFragment(new StoreOwnerDashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();
            if (id == R.id.nav_owner_dashboard) {
                fragment = new StoreOwnerDashboardFragment();
            } else if (id == R.id.nav_owner_foods) {
                fragment = new ManageFoodsFragment();
            } else if (id == R.id.nav_owner_orders) {
                fragment = new StoreOrdersFragment();
            } else {
                // nav_owner_profile — tái sử dụng ProfileFragment hiện có
                fragment = new ProfileFragment();
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

    /** Các Fragment con có thể gọi để lấy storeId */
    public String getStoreId() {
        return storeId;
    }
}
