package com.tastenfood.FoodApp.category.services;

import com.tastenfood.FoodApp.category.dtos.CategoryDTO;
import com.tastenfood.FoodApp.response.Response;

import java.util.List;

public interface CategoryService {
    Response<CategoryDTO> addCategory(CategoryDTO categoryDTO);
    Response<CategoryDTO> updateCategory(CategoryDTO categoryDTO);
    Response<CategoryDTO> getCategory(Long id);
    Response<List<CategoryDTO>> getAllCategories();
    Response<?> deleteCategory(Long id);
}