package com.backend.videostreaming.repositories;

import com.backend.videostreaming.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // This method defines a query to find a User entity by its email address.
    // Using Optional is significant because it elegantly handles the case where no user is found with the given email.
    public Optional<User> findByEmail(String email);
    Optional<User> findByResetPasswordToken(String token);
}
