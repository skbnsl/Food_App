package com.tastenfood.FoodApp.cart.repository;

import com.tastenfood.FoodApp.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
