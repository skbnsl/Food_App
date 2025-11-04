package com.tastenfood.FoodApp.auth_users.services;

import com.tastenfood.FoodApp.auth_users.dtos.UserDTO;
import com.tastenfood.FoodApp.auth_users.entity.User;
import com.tastenfood.FoodApp.auth_users.repository.UserRepository;
import com.tastenfood.FoodApp.aws.AWSS3Service;
import com.tastenfood.FoodApp.email_notification.dtos.NotificationDTO;
import com.tastenfood.FoodApp.email_notification.services.NotificationService;
import com.tastenfood.FoodApp.exceptions.BadRequestException;
import com.tastenfood.FoodApp.exceptions.NotFoundException;
import com.tastenfood.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final AWSS3Service awss3Service;

    @Override
    public User getCurrentLoggedInUser() {
        log.info("Inside getCurrentLoggedInUser");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(()->new NotFoundException("User Not Found: getCurrentLoggedInUser"));
    }

    @Override
    public Response<List<UserDTO>> getAllUsers() {
        log.info("Inside getAllUsers");
        List<User> userList = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        //this is for iterate on every user that convert it into dto
        List<UserDTO> userDTOS = modelMapper.map(userList, new TypeToken<List<UserDTO>>(){}.getType());
        return Response.<List<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All Users retrieved successfully!")
                .data(userDTOS)
                .build();
    }

    @Override
    public Response<UserDTO> getOwnAccountDetails() {
        log.info("Inside getOwnAccountDetails");
        User user = getCurrentLoggedInUser();
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .data(userDTO)
                .build();
    }

    @Override
    public Response<?> updateOwnAccount(UserDTO userDTO) {
        log.info("Inside updateOwnAccount");
        //fetch current logged in user
        User user = getCurrentLoggedInUser();
        String profileUrl = user.getProfileUrl();
        MultipartFile imageFile = userDTO.getImageFile();

        //check if new image file provided
        if(imageFile!=null && !imageFile.isEmpty()){
            //delete old image in cloud if exists
            if(profileUrl!=null && !profileUrl.isEmpty()){
                String keyName = profileUrl.substring(profileUrl.lastIndexOf("/")+1);
                awss3Service.deleteFile("/profile"+keyName);
                log.info("Deleted old profile image from s3: {}", keyName);
            }
            //upload new image
            String imageName = UUID.randomUUID().toString() + "_" +imageFile.getOriginalFilename();
            URL newImageUrl = awss3Service.uploadFile("profile/"+imageName,imageFile);
            user.setProfileUrl(newImageUrl.toString());
        }
        //update user details
        if(userDTO.getName() != null) user.setName(userDTO.getName());
        if(userDTO.getPhoneNumber() != null) user.setPhoneNumber(userDTO.getPhoneNumber());
        if(userDTO.getAddress() != null) user.setAddress(userDTO.getAddress());
        if(userDTO.getPassword() != null) user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        if(userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())){
            //check new email is already taken
            if(userRepository.existsByEmail(userDTO.getEmail())){
                throw new BadRequestException("updateOwnAccount: Email already exist!");
            }
            user.setEmail(userDTO.getEmail());
        }
        user.setEmail(userDTO.getEmail());

        //update the user
        userRepository.save(user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account updated successfully!")
                .build();
    }


    @Override
    public Response<?> deactivateOwnAccount() {
        log.info("inside deactivateOwnAccount");
        User user = getCurrentLoggedInUser();
        //deactivate the user
        user.setActive(false);
        userRepository.save(user);

        //send email after deactivation
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Account Deactivated")
                .body("Your account is deactivated! " +
                        "if this was mistake, please contact to support!")
                .build();
        notificationService.sendmail(notificationDTO);

        //return response
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account deactivated successfully!")
                .build();
    }
}
