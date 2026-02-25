package com.example.foodnow;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.foodnow.fragments.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ① Tìm BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // ② Hiển thị HomeFragment mặc định khi mở app
        loadFragment(new HomeFragment());

        // ③ Lắng nghe sự kiện khi người dùng bấm tab
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_orders) {
                // Sau này thay bằng: new OrdersFragment()
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_favorites) {
                // Sau này thay bằng: new FavoritesFragment()
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_profile) {
                // Sau này thay bằng: new ProfileFragment()
                selectedFragment = new HomeFragment();
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
}
