package com.example.foodnow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodnow.models.Category;
import com.example.foodnow.repositories.CategoryRepository;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final LiveData<List<Category>> categories;

    public HomeViewModel() {
        CategoryRepository categoryRepo = new CategoryRepository();
        categories = categoryRepo.getAllCategories();
    }

    public LiveData<List<Category>> getCategories() { return categories; }
}
