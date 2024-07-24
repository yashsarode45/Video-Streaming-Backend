package com.backend.videostreaming.controllers;

import com.backend.videostreaming.DTOs.UserDTO;
import com.backend.videostreaming.entities.User;
import com.backend.videostreaming.models.JwtRequest;
import com.backend.videostreaming.models.JwtResponse;
import com.backend.videostreaming.repositories.UserRepository;
import com.backend.videostreaming.security.JwtHelper;
import com.backend.videostreaming.services.EmailService;
import com.backend.videostreaming.services.OTPService;
import com.backend.videostreaming.services.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AuthController {

    @Autowired
    private UserDetailsService userDetailsService; // to get user information

    @Autowired
    private AuthenticationManager manager;


    @Autowired
    private JwtHelper helper;

    private Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailService emailService;

    // we get email and password in the request
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {

        this.doAuthenticate(request.getEmail(), request.getPassword());


        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = this.helper.generateToken(userDetails);

        JwtResponse response = JwtResponse.builder()
                .jwtToken(token)
                .username(userDetails.getUsername()).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void doAuthenticate(String email, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
           // AuthenticationManager processes the authentication token and attempts to authenticate the user.
           // If the authentication is successful, the method returns without any exception.
           // If the authentication fails (e.g., due to invalid credentials), a BadCredentialsException is thrown.
            manager.authenticate(authentication);


        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(" Invalid Username or Password  !!"); // logs only in logs not sent as a response, thus we wrote the exception handler below
        }

    }

    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Credentials Invalid !!";
    }


    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("Sending OTP to email: {}", email);
        Map<String, Object> response = new HashMap<>();

        try {
            if (userRepository.findByEmail(email).isPresent()) {
                response.put("success", false);
                response.put("message", "Email already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            String otp = otpService.generateAndSaveOTP(email);
            response.put("success", true);
            response.put("message", "OTP sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send OTP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody User user, @RequestParam String otp) {
        Map<String, Object> response = new HashMap<>();
        logger.info("In /signup with user {}", user);
        try {
            if (!otpService.validateOTP(user.getEmail(), otp)) {
                response.put("success", false);
                response.put("message", "Invalid or Expired OTP");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User newUser = userService.createUser(user);
            UserDTO newUserDto = userService.convertToUserDTO(newUser);
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("user", newUserDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to register user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/reset-password-token")
    public ResponseEntity<Map<String, Object>> resetPasswordToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String email = request.get("email");

        try {
            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is empty");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userService.findByEmail(email);
            String token = UUID.randomUUID().toString();
            userService.updateResetPasswordToken(email, token);

            String resetUrl = "http://localhost:5173/update-password/" + token;
            emailService.sendPasswordResetEmail(email, "Password Reset Link", "Link will expire in 5 min" +"Password reset link: " + resetUrl);

            response.put("success", true);
            response.put("message", "Reset link sent");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String token = request.get("token");
        String password = request.get("password");
        String confirmPassword = request.get("confirmPassword");

        try {
            if (token == null || password == null || confirmPassword == null) {
                response.put("success", false);
                response.put("message", "Enter all details");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userService.findByResetPasswordToken(token);

            if (user.getResetPasswordExpires().isBefore(LocalDateTime.now())) {
                response.put("success", false);
                response.put("message", "Token is no longer valid");
                return ResponseEntity.badRequest().body(response);
            }

            if (!password.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "Passwords don't match");
                return ResponseEntity.badRequest().body(response);
            }

            userService.updatePassword(user, password);

            response.put("success", true);
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}

