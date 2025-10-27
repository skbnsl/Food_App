package com.tastenfood.FoodApp.review.repository;

import com.tastenfood.FoodApp.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMenuIdOrderByIdDesc(Long menuId);

    @Query("select avg(r.rating) from review r where e.menu.id = :menuId" )
    Double calculateAverageRatingByMenuId(@Param("menuId") Long menuId);


    @Query("select case when count(r) > 0 then true else false" +
        " from review r " +
    "where r.user.id = :userId and r.menu.id = :menuId and r.order.id = :orderId")
    boolean existsByUserIdAndMenuIdAndOrderId(
            @Param("userId") Long userId,
            @Param("menuId") Long menuId,
            @Param("orderId") Long orderId
    );

}
