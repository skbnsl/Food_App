package com.tastenfood.FoodApp.order.repository;


import com.tastenfood.FoodApp.auth_users.entity.User;
import com.tastenfood.FoodApp.enums.OrderStatus;
import com.tastenfood.FoodApp.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    List<Order> findByUserOrderByOrderDateDesc(User user);

    @Query("select count(distinct o.user.id) from order o")
    long countDistinctUsers();

}
