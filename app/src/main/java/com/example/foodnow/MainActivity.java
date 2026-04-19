package com.example.foodnow;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.foodnow.fragments.CartFragment;
import com.example.foodnow.fragments.FavoritesFragment;
import com.example.foodnow.fragments.HomeFragment;
import com.example.foodnow.fragments.OrdersFragment;
import com.example.foodnow.fragments.ProfileFragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ① Tìm BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        setupCartBadge(bottomNav);

        // ② Hiển thị HomeFragment mặc định khi mở app
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // ③ Lắng nghe sự kiện khi người dùng bấm tab
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (id == R.id.nav_orders) {
                selectedFragment = new OrdersFragment();
            } else if (id == R.id.nav_favorites) {
                selectedFragment = new FavoritesFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    // Hàm tiện ích: đặt Fragment vào fragment_container
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void setupCartBadge(BottomNavigationView bottomNav) {
        BadgeDrawable badgeDrawable = bottomNav.getOrCreateBadge(R.id.nav_cart);
        badgeDrawable.setVisible(true);
        badgeDrawable.setNumber(3);
        badgeDrawable.setBackgroundColor(ContextCompat.getColor(this, R.color.home_primary_orange));
        badgeDrawable.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));
    }
}
