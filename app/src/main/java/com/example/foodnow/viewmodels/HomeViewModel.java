package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Category;
import com.example.foodnow.models.Food;
import com.example.foodnow.models.Store;
import com.example.foodnow.repositories.CategoryRepository;
import com.example.foodnow.repositories.FoodRepository;
import com.example.foodnow.repositories.StoreRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends ViewModel {

    private final LiveData<List<Category>> categories;
    private final LiveData<List<Store>> stores;
    private final LiveData<List<Food>> topRatedFoods;
    private final MutableLiveData<Map<String, String>> storeNamesMap = new MutableLiveData<>(new HashMap<>());

    private final StoreRepository storeRepository;

    public HomeViewModel() {
        CategoryRepository categoryRepo = new CategoryRepository();
        categories = categoryRepo.getAllCategories();

        storeRepository = new StoreRepository();
        stores = storeRepository.getAllStores();

        FoodRepository foodRepo = new FoodRepository();
        topRatedFoods = foodRepo.getTopRatedFoods();
    }

    public LiveData<List<Category>> getCategories() { return categories; }

    public LiveData<List<Store>> getStores() { return stores; }

    public LiveData<List<Food>> getTopRatedFoods() { return topRatedFoods; }

    public LiveData<Map<String, String>> getStoreNamesMap() { return storeNamesMap; }

    /** Gọi từ Fragment sau khi foods đã load — batch fetch tên quán */
    public void fetchStoreNamesForFoods(List<Food> foods) {
        if (foods == null || foods.isEmpty()) return;
        List<String> ids = new ArrayList<>();
        for (Food f : foods) {
            if (f.getStoreId() != null && !f.getStoreId().isEmpty()
                    && !ids.contains(f.getStoreId())) {
                ids.add(f.getStoreId());
            }
        }
        storeRepository.getStoreNamesByIds(ids)
                .addOnSuccessListener(storeNamesMap::setValue)
                .addOnFailureListener(e -> storeNamesMap.setValue(new HashMap<>()));
    }
}

