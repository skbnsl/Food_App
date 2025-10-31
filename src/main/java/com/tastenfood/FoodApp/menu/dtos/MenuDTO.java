package com.tastenfood.FoodApp.menu.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tastenfood.FoodApp.category.entity.Category;
import com.tastenfood.FoodApp.review.dtos.ReviewDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class MenuDTO {

    private Long id;

    @NotBlank(message = "name is required")
    private String name;

    private String description;
    private String imageUrl;

    @NotNull(message = "price is required")
    @Positive(message = "price must be positive")
    private BigDecimal price;

    @NotNull(message = "category Id is required")
    private Long categoryId;

    private MultipartFile imageFile;

    private List<ReviewDTO> reviews;

}
