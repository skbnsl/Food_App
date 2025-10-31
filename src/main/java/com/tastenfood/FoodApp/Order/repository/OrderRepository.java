package com.tastenfood.FoodApp.Order.repository;


import com.tastenfood.FoodApp.Order.entity.Order;
import com.tastenfood.FoodApp.auth_users.entity.User;
import com.tastenfood.FoodApp.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    List<Order> findByUserOrderByOrderDateDesc(User user);

    @Query("select count(distinct o.user.id) from Order o")
    long countDistinctUsers();

}
