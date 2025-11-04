package com.tastenfood.FoodApp.auth_users.services;

import com.tastenfood.FoodApp.auth_users.dtos.LoginRequest;
import com.tastenfood.FoodApp.auth_users.dtos.LoginResponse;
import com.tastenfood.FoodApp.auth_users.dtos.RegistrationRequest;
import com.tastenfood.FoodApp.response.Response;

public interface AuthService {

    Response<?> register(RegistrationRequest registrationRequest);

    Response<LoginResponse> login(LoginRequest loginRequest);

}
