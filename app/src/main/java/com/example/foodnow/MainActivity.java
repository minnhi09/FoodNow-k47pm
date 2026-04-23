package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.foodnow.fragments.CartFragment;
import com.example.foodnow.fragments.FavoritesFragment;
import com.example.foodnow.fragments.HomeFragment;
import com.example.foodnow.fragments.OrdersFragment;
import com.example.foodnow.fragments.ProfileFragment;
import com.example.foodnow.utils.CartManager;
import com.example.foodnow.utils.CloudinaryHelper;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private final CartManager.OnCartChangedListener cartChangedListener = this::updateCartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo Cloudinary — chỉ 1 lần khi app mở
        CloudinaryHelper.init(getApplicationContext());

        setContentView(R.layout.activity_main);

        // ① Tìm BottomNavigationView
        bottomNav = findViewById(R.id.bottom_navigation);

        setupCartBadge(bottomNav);

        // ② Hiển thị tab khởi tạo
        int initialTab = resolveInitialTab(getIntent());
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(initialTab);
            loadFragment(fragmentForTab(initialTab));
        }

        // ③ Lắng nghe sự kiện khi người dùng bấm tab
        bottomNav.setOnItemSelectedListener(item -> {
            return loadFragment(fragmentForTab(item.getItemId()));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        CartManager.getInstance().registerListener(cartChangedListener);
        updateCartBadge();
    }

    @Override
    protected void onStop() {
        CartManager.getInstance().unregisterListener(cartChangedListener);
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int tab = resolveInitialTab(intent);
        if (bottomNav != null) bottomNav.setSelectedItemId(tab);
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
        badgeDrawable.setBackgroundColor(ContextCompat.getColor(this, R.color.home_primary_orange));
        badgeDrawable.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));
        updateCartBadge();
    }

    private void updateCartBadge() {
        if (bottomNav == null) return;
        int count = CartManager.getInstance().getItemCount();
        BadgeDrawable badgeDrawable = bottomNav.getOrCreateBadge(R.id.nav_cart);
        if (count <= 0) {
            badgeDrawable.clearNumber();
            badgeDrawable.setVisible(false);
            return;
        }
        badgeDrawable.setVisible(true);
        badgeDrawable.setNumber(Math.min(count, 99));
    }

    private int resolveInitialTab(Intent intent) {
        if (intent == null) return R.id.nav_home;
        String openTab = intent.getStringExtra("open_tab");
        if ("cart".equals(openTab)) return R.id.nav_cart;
        if ("orders".equals(openTab)) return R.id.nav_orders;
        return R.id.nav_home;
    }

    private Fragment fragmentForTab(int id) {
        if (id == R.id.nav_home) return new HomeFragment();
        if (id == R.id.nav_cart) return new CartFragment();
        if (id == R.id.nav_orders) return new OrdersFragment();
        if (id == R.id.nav_favorites) return new FavoritesFragment();
        if (id == R.id.nav_profile) return new ProfileFragment();
        return new HomeFragment();
    }
}
