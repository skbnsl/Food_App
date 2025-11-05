package com.tastenfood.FoodApp.category.services;

import com.tastenfood.FoodApp.category.dtos.CategoryDTO;
import com.tastenfood.FoodApp.category.entity.Category;
import com.tastenfood.FoodApp.category.repository.CategoryRepository;
import com.tastenfood.FoodApp.exceptions.NotFoundException;
import com.tastenfood.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<CategoryDTO> addCategory(CategoryDTO categoryDTO) {
        log.info("inside addCategory: {}", categoryDTO.toString());
        Category category = modelMapper.map(categoryDTO, Category.class);
        categoryRepository.save(category);
        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Category added successfully!")
                .build();
    }

    @Override
    public Response<CategoryDTO> updateCategory(CategoryDTO categoryDTO) {
        log.info("inside updateCategory: {}", categoryDTO.toString());
        Long id = categoryDTO.getId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("category not exist with id: " + id.toString()));

        if(categoryDTO.getName() != null && !categoryDTO.getName().isEmpty())  category.setName(categoryDTO.getName());
        if(categoryDTO.getDescription() != null && !categoryDTO.getDescription().isEmpty())  category.setDescription(categoryDTO.getDescription());

        categoryRepository.save(category);

        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Category updated successfully!")
                .build();
    }

    @Override
    public Response<CategoryDTO> getCategory(Long id) {
        log.info("inside getCategory: {}", id.toString());
        Category category = categoryRepository.findById(id).orElseThrow(()->new NotFoundException("Category Not Found :"+id));
        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);
        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Category retrieved successfully!")
                .data(categoryDTO)
                .build();
    }

    @Override
    public Response<List<CategoryDTO>> getAllCategories() {
        log.info("inside getAllCategories");
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class)).toList();
        return Response.<List<CategoryDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All Categories retrieved successfully!")
                .data(categoryDTOS)
                .build();
    }

    @Override
    public Response<?> deleteCategory(Long id) {
        log.info("inside deleteCategory: {}", id.toString());
        if(!categoryRepository.existsById(id)){
            throw new NotFoundException("Category Not Found :"+id);
        }
        categoryRepository.deleteById(id);
        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Category deleted successfully!")
                .build();
    }
}
