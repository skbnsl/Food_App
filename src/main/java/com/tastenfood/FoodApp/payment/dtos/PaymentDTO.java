package com.tastenfood.FoodApp.payment.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tastenfood.FoodApp.Order.dtos.OrderDTO;
import com.tastenfood.FoodApp.auth_users.dtos.UserDTO;
import com.tastenfood.FoodApp.enums.PaymentGateway;
import com.tastenfood.FoodApp.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDTO {

    private Long id;

    private Long orderId;

    private BigDecimal amount;

    private PaymentStatus paymentStatus;

    private String transactionId;

    private PaymentGateway paymentGateway;

    private String failureReason;

    private boolean success;

    private LocalDateTime paymentDate;

    private OrderDTO order;
    private UserDTO user;

}
