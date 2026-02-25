package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Food;
import com.example.foodnow.repositories.FoodRepository;

import java.util.List;

public class StoreDetailViewModel extends ViewModel {

    private final FoodRepository foodRepository;
    private LiveData<List<Food>> foods;

    public StoreDetailViewModel() {
        foodRepository = new FoodRepository();
    }

    /** Gọi để load danh sách món của 1 quán */
    public LiveData<List<Food>> getFoods(String storeId) {
        if (foods == null) {
            foods = foodRepository.getFoodsByStore(storeId);
        }
        return foods;
    }
}
