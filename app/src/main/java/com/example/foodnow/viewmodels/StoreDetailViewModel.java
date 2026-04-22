package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Food;
import com.example.foodnow.repositories.FoodRepository;

import java.util.List;

public class StoreDetailViewModel extends ViewModel {

    private LiveData<List<Food>> foods;

    // Lazy init: chỉ query Firestore một lần cho mỗi storeId
    public LiveData<List<Food>> getFoods(String storeId) {
        if (foods == null) {
            FoodRepository repo = new FoodRepository();
            foods = repo.getFoodsByStore(storeId);
        }
        return foods;
    }
}
