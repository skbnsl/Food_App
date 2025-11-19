package com.tastenfood.FoodApp.Order.services;

import com.tastenfood.FoodApp.Order.dtos.OrderDTO;
import com.tastenfood.FoodApp.Order.dtos.OrderItemDTO;
import com.tastenfood.FoodApp.enums.OrderStatus;
import com.tastenfood.FoodApp.response.Response;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    Response<?> placeOrderFromCart();
    Response<OrderDTO> getOrderById(Long id);
    Response<Page<OrderDTO>> getAllOrders(OrderStatus orderStatus, int page, int size);
    Response<List<OrderDTO>> getOrdersOfUser();
    Response<OrderItemDTO> getOrderItemById(Long orderItemId);
    Response<OrderDTO> updateOrderStatus(OrderDTO orderDTO);
    Response<Long> countUniqueCustomers();
}
