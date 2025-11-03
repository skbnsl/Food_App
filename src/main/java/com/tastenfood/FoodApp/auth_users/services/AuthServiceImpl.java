package com.tastenfood.FoodApp.auth_users.services;

import com.tastenfood.FoodApp.auth_users.dtos.LoginRequest;
import com.tastenfood.FoodApp.auth_users.dtos.RegistrationRequest;
import com.tastenfood.FoodApp.auth_users.dtos.UserDTO;
import com.tastenfood.FoodApp.auth_users.entity.User;
import com.tastenfood.FoodApp.auth_users.repository.UserRepository;
import com.tastenfood.FoodApp.exceptions.BadRequestException;
import com.tastenfood.FoodApp.exceptions.NotFoundException;
import com.tastenfood.FoodApp.response.Response;
import com.tastenfood.FoodApp.role.entity.Role;
import com.tastenfood.FoodApp.role.repository.RoleRepository;
import com.tastenfood.FoodApp.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;
    private final ModelMapper mapper;

    @Override
    public Response<?> register(RegistrationRequest registrationRequest) {
        log.info("inside user register: {}", registrationRequest.getAddress());
        if(userRepository.existsByEmail(registrationRequest.getEmail())){
            throw new BadRequestException("Email Already Exists!");
        }
        //collect all roles from the request
        List<Role> userRoles;
        if(registrationRequest.getRoles() != null && !registrationRequest.getRoles().isEmpty()){
            userRoles = registrationRequest.getRoles().stream()
                    .map(role -> roleRepository.findByName(role.getName().toUpperCase())
                            .orElseThrow(()-> new NotFoundException("Role with name: "+role.getName()+" not found!")))
                    .toList();
        } else {
            //if no roles provided, default to customer
            Role defaultRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new NotFoundException("Default CUSTOMER role not found!"));
            userRoles = List.of(defaultRole);
        }

        //build the user object
        User userToSave = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .address(registrationRequest.getAddress())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .roles(userRoles)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        //save the user
        userRepository.save(userToSave);

        log.info("user registered");
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("user register successfully")
                .data(mapper.map(userToSave, UserDTO.class))
                .build();
    }


    @Override
    public Response<?> login(LoginRequest loginRequest) {

        log.info("inside login request:"+loginRequest.getEmail());

    }
}
