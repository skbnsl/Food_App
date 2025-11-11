package com.tastenfood.FoodApp.cart.service;

import com.tastenfood.FoodApp.auth_users.entity.User;
import com.tastenfood.FoodApp.auth_users.services.UserService;
import com.tastenfood.FoodApp.cart.dtos.CartDTO;
import com.tastenfood.FoodApp.cart.entity.Cart;
import com.tastenfood.FoodApp.cart.entity.CartItem;
import com.tastenfood.FoodApp.cart.repository.CartItemRepository;
import com.tastenfood.FoodApp.cart.repository.CartRepository;
import com.tastenfood.FoodApp.exceptions.NotFoundException;
import com.tastenfood.FoodApp.menu.entity.Menu;
import com.tastenfood.FoodApp.menu.repository.MenuRepository;
import com.tastenfood.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService{

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final MenuRepository menuRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public Response<?> addItemToCart(CartDTO cartDTO) {
        log.info("Inside addItemToCart: cartDto {}", cartDTO.toString());

        Long menuId = cartDTO.getMenuId();
        int quantity = cartDTO.getQuantity();

        User user = userService.getCurrentLoggedInUser();
        Menu menu = menuRepository.findById(menuId).orElseThrow(() ->
                new NotFoundException("Menu Item Not Found with menuId="+menuId));

        Cart cart = cartRepository.findByUser_Id(user.getId()).orElseGet(() -> {
            //if cart does not exist
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setCartItems(new ArrayList<>());
            return cartRepository.save(newCart);
        });

        //check if cart already exist in the cart
        Optional<CartItem> optionalCartItem = cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getMenu().getId().equals(menuId))
                .findFirst();

        // exist, then increment
        if (optionalCartItem.isPresent()){
            CartItem cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            cartItemRepository.save(cartItem);
        } else {
            //if not present
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .menu(menu)
                    .quantity(quantity)
                    .pricePerUnit(menu.getPrice())
                    .subTotal(menu.getPrice().multiply(BigDecimal.valueOf(quantity)))
                    .build();
            cart.getCartItems().add(newCartItem);
            cartItemRepository.save(newCartItem);
        }

        //cartRepository.save(cart);//not, it will auto save and persists in the cart table

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item added successfully!")
                .build();
    }

    @Override
    public Response<?> incrementItem(Long menuId) {
        log.info("Inside incrementItem: menuId {}", menuId);

        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElseThrow(() ->
                new NotFoundException("cart not found with userId:"+user.getId()));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst().orElseThrow(() -> new NotFoundException("Menu not found in cart"));

        int newQuantity = cartItem.getQuantity() + 1;

        cartItem.setQuantity(newQuantity);
        cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(newQuantity)));
        cartItemRepository.save(cartItem);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item quantity increased successfully!")
                .build();

    }

    @Override
    public Response<?> decrementItem(Long menuId) {
        log.info("Inside decrementItem: menuId {}", menuId);
        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElseThrow(() ->
                new NotFoundException("cart not found with userId:"+user.getId()));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst().orElseThrow(() -> new NotFoundException("Menu not found in cart"));

        int newQuantity = cartItem.getQuantity() - 1; //decrement
        if(newQuantity > 0){
            cartItem.setQuantity(newQuantity);
            cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(newQuantity)));
            cartItemRepository.save(cartItem);
        } else {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        }

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item quantity decreased successfully!")
                .build();

    }

    @Override
    public Response<?> removeItem(Long cartItemId) {
        log.info("Inside removeItem: cartItemId {}", cartItemId);
        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElseThrow(() ->
                new NotFoundException("cart not found with userId:"+user.getId()));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(()-> new NotFoundException("cart item not found!"));

        if(!cart.getCartItems().contains(cartItem)){
            throw new NotFoundException("cart item does not belong to this user's cart");
        }
        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item removed from cart successfully!")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<CartDTO> getShoppingCart() {
        log.info("Inside getShoppingCart:");
        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElseThrow(() ->
                new NotFoundException("cart not found with userId:"+user.getId()));
        List<CartItem> cartItems = cart.getCartItems();
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        //calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (cartItems != null){
            for (CartItem item : cartItems){
                totalAmount = totalAmount.add(item.getSubTotal());
            }
        }
        cartDTO.setTotalAmount(totalAmount); // set the totalAmount

        //remove review from response
        if(cartDTO.getCartItems() != null){
            cartDTO.getCartItems()
                    .forEach(item -> item.getMenu().setReviews(null));
        }

        return Response.<CartDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("shopping cart retrieved successfully!")
                .data(cartDTO)
                .build();
    }

    @Override
    public Response<?> clearShoppingCart() {
        log.info("Inside clearShoppingCart: cartDTO ");
        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId()).orElseThrow(() ->
                new NotFoundException("cart not found with userId:"+user.getId()));

        //delete cart items from db first
        cartItemRepository.deleteAll(cart.getCartItems());

        //clearing the cart's items collection
        cart.getCartItems().clear();

        //save the cart to update the db
        cartRepository.save(cart);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("shopping cart cleared successfully!")
                .build();
    }
}
