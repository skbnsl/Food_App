package com.tastenfood.FoodApp.payment.repository;

import com.tastenfood.FoodApp.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
