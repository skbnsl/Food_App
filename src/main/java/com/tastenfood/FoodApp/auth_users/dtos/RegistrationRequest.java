package com.tastenfood.FoodApp.auth_users.dtos;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RegistrationRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "invalid email format")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 3, message = "password must be at least 3 characters long")
    private String password;

    @NotBlank(message = "address is required")
    private String address;

    @NotBlank(message = "phone Number is required")
    private String phoneNumber;

    private List<Role> roles;
}
