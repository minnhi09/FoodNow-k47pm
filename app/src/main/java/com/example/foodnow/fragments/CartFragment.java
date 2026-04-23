package com.example.foodnow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.activities.CheckoutActivity;
import com.example.foodnow.adapters.CartAdapter;
import com.example.foodnow.models.CartItem;
import com.example.foodnow.utils.CartManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private TextView tvCartStoreName;
    private RecyclerView rvCart;
    private TextView tvCartEmpty;
    private LinearLayout layoutCartFooter;
    private TextView tvSubtotal;
    private Button btnCheckout;

    private CartAdapter adapter;
    private List<CartItem> cartList;
    private NumberFormat numberFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCartStoreName = view.findViewById(R.id.tv_cart_store_name);
        rvCart = view.findViewById(R.id.rv_cart);
        tvCartEmpty = view.findViewById(R.id.tv_cart_empty);
        layoutCartFooter = view.findViewById(R.id.layout_cart_footer);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        btnCheckout = view.findViewById(R.id.btn_checkout);

        numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        cartList = CartManager.getInstance().getItems();
        adapter = new CartAdapter(requireContext(), cartList, this::updateCartUI);

        rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCart.setAdapter(adapter);

        btnCheckout.setOnClickListener(v ->
                startActivity(new Intent(getContext(), CheckoutActivity.class))
        );

        updateCartUI();
    }

    private void updateCartUI() {
        adapter.notifyDataSetChanged();

        String storeName = CartManager.getInstance().getCurrentStoreName();
        if (storeName == null || storeName.trim().isEmpty()) {
            tvCartStoreName.setText("Chưa chọn quán");
        } else {
            tvCartStoreName.setText(storeName);
        }

        long subtotal = CartManager.getInstance().getSubtotal();
        tvSubtotal.setText("Tạm tính: " + numberFormat.format(subtotal) + "đ");

        boolean isEmpty = CartManager.getInstance().getItems().isEmpty();
        tvCartEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        layoutCartFooter.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
