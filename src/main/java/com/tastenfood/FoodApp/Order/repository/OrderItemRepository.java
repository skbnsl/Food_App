package com.tastenfood.FoodApp.Order.repository;


import com.tastenfood.FoodApp.Order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {

    @Query("select case when count(oi)>0 then true else false end "+
    " from OrderItem oi " +
    "where oi.order.id = :orderId and oi.menu.id = :menuId")
    boolean existsByOrderIdAndMenuId(@Param("orderId") Long orderId,
                                     @Param("menuId") Long menuId);

}
