package com.example.foodnow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.activities.CheckoutActivity;
import com.example.foodnow.adapters.CartAdapter;
import com.example.foodnow.utils.CartManager;

import java.text.NumberFormat;
import java.util.Locale;

public class CartFragment extends Fragment {

    private RecyclerView rvCart;
    private CartAdapter cartAdapter;
    private TextView tvStoreName, tvSubtotal, tvCartEmpty;
    private LinearLayout layoutFooter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        rvCart       = view.findViewById(R.id.rv_cart);
        tvStoreName  = view.findViewById(R.id.tv_cart_store_name);
        tvSubtotal   = view.findViewById(R.id.tv_subtotal);
        tvCartEmpty  = view.findViewById(R.id.tv_cart_empty);
        layoutFooter = view.findViewById(R.id.layout_cart_footer);

        CartManager cart = CartManager.getInstance();

        cartAdapter = new CartAdapter(getContext(), cart.getItems(), this::updateUI);
        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCart.setAdapter(cartAdapter);

        // Nút đặt hàng
        view.findViewById(R.id.btn_checkout).setOnClickListener(v -> {
            if (cart.getItemCount() == 0) {
                Toast.makeText(getContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getContext(), "Chuyển đến thanh toán...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getContext(), CheckoutActivity.class));
        });

        updateUI();
        return view;
    }

    private void updateUI() {
        CartManager cart = CartManager.getInstance();
        boolean empty = cart.getItemCount() == 0;

        rvCart.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvCartEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        layoutFooter.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (!empty) {
            tvStoreName.setText("Quán: " + cart.getCurrentStoreName());
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvSubtotal.setText("Tổng: " + nf.format(cart.getSubtotal()) + "đ");
        }
    }
}
