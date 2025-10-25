package com.tastenfood.FoodApp.auth_users.repository;

import com.tastenfood.FoodApp.auth_users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existByEmail(String email);

}
