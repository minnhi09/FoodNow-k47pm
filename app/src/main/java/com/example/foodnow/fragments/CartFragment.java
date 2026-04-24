package com.example.foodnow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.MainActivity;
import com.example.foodnow.R;
import com.example.foodnow.activities.CheckoutActivity;
import com.example.foodnow.adapters.CartAdapter;
import com.example.foodnow.utils.CartManager;

import java.text.NumberFormat;
import java.util.Locale;

public class CartFragment extends Fragment {

    private RecyclerView rvCart;
    private TextView tvCartCountHeader, tvSubtotal, tvDeliveryFee, tvTotal, tvTotalButton, tvEmpty;
    private View btnCheckout, layoutPaymentDetails, cardPromo, cardPaymentMethod;
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rvCart = view.findViewById(R.id.rv_cart);
        tvCartCountHeader = view.findViewById(R.id.tv_cart_count_header);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvDeliveryFee = view.findViewById(R.id.tv_delivery_fee);
        tvTotal = view.findViewById(R.id.tv_total);
        tvTotalButton = view.findViewById(R.id.tv_total_button);
        tvEmpty = view.findViewById(R.id.tv_cart_empty);
        btnCheckout = view.findViewById(R.id.btn_checkout);
        layoutPaymentDetails = view.findViewById(R.id.layout_payment_details);
        cardPromo = view.findViewById(R.id.card_promo);
        cardPaymentMethod = view.findViewById(R.id.card_payment_method);

        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        
        btnCheckout.setOnClickListener(v -> {
            if (!CartManager.getInstance().getItems().isEmpty()) {
                startActivity(new Intent(getContext(), CheckoutActivity.class));
            }
        });

        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        CartManager cart = CartManager.getInstance();
        int count = cart.getItemCount();
        
        tvCartCountHeader.setText(getString(R.string.cart_item_count_format, count));
        
        if (cart.getItems().isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
            layoutPaymentDetails.setVisibility(View.GONE);
            cardPromo.setVisibility(View.GONE);
            cardPaymentMethod.setVisibility(View.GONE);
            btnCheckout.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvCart.setVisibility(View.VISIBLE);
            layoutPaymentDetails.setVisibility(View.VISIBLE);
            cardPromo.setVisibility(View.VISIBLE);
            cardPaymentMethod.setVisibility(View.VISIBLE);
            btnCheckout.setVisibility(View.VISIBLE);

            CartAdapter adapter = new CartAdapter(getContext(), cart.getItems(), this::updatePrices);
            rvCart.setAdapter(adapter);
            updatePrices();
        }
        refreshCartBadge();
    }

    private void updatePrices() {
        CartManager cart = CartManager.getInstance();
        if (cart.getItems().isEmpty()) {
            updateUI();
            return;
        }

        long subtotalValue = cart.getSubtotal();
        long deliveryFeeValue = 15000L; // Phí ship cố định
        long totalValue = subtotalValue + deliveryFeeValue;

        tvSubtotal.setText(nf.format(subtotalValue) + "đ");
        tvDeliveryFee.setText(nf.format(deliveryFeeValue) + "đ");
        tvTotal.setText(nf.format(totalValue) + "đ");
        tvTotalButton.setText(nf.format(totalValue) + "đ");
        
        int count = cart.getItemCount();
        tvCartCountHeader.setText(getString(R.string.cart_item_count_format, count));
        
        refreshCartBadge();
    }

    private void refreshCartBadge() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshCartBadge();
        }
    }
}
