package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Category;
import com.example.foodnow.models.Store;
import com.example.foodnow.repositories.CategoryRepository;
import com.example.foodnow.repositories.StoreRepository;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final LiveData<List<Store>> stores;
    private final LiveData<List<Category>> categories;

    public HomeViewModel() {
        StoreRepository storeRepo = new StoreRepository();
        CategoryRepository categoryRepo = new CategoryRepository();
        stores     = storeRepo.getAllStores();
        categories = categoryRepo.getAllCategories();
    }

    public LiveData<List<Store>> getStores()         { return stores; }
    public LiveData<List<Category>> getCategories()  { return categories; }
}
