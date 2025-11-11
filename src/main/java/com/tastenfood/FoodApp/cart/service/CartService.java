package com.tastenfood.FoodApp.cart.service;

import com.tastenfood.FoodApp.cart.dtos.CartDTO;
import com.tastenfood.FoodApp.response.Response;

public interface CartService {

    Response<?> addItemToCart(CartDTO cartDTO);
    Response<?> incrementItem(Long menuId);
    Response<?> decrementItem(Long menuId);
    Response<?> removeItem(Long cartItemId);
    Response<CartDTO> getShoppingCart();
    Response<?> clearShoppingCart();

}