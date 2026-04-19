package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Category;
import com.example.foodnow.models.Store;
import com.example.foodnow.repositories.CategoryRepository;
import com.example.foodnow.repositories.StoreRepository;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final LiveData<List<Category>> categories;
    private final LiveData<List<Store>> stores;

    public HomeViewModel() {
        CategoryRepository categoryRepo = new CategoryRepository();
        categories = categoryRepo.getAllCategories();


        // Thêm mới
        StoreRepository storeRepo = new StoreRepository();
        stores = storeRepo.getAllStores();
    }

    public LiveData<List<Category>> getCategories() { return categories; }

    // Getter mới cho stores
    public LiveData<List<Store>> getStores() {
        return stores;
    }
}
