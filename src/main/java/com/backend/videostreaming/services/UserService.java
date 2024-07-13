package com.backend.videostreaming.services;

import com.backend.videostreaming.DTOs.UserDTO;
import com.backend.videostreaming.DTOs.VideoDTO;
import com.backend.videostreaming.entities.User;
import com.backend.videostreaming.entities.Video;
import com.backend.videostreaming.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserDTO> getUserDTOs() {
        return userRepository.findAll().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    public User createUser(User user) {
        user.setUserId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
    }

    public void updateResetPasswordToken(String email, String token) {
        User user = findByEmail(email);
        user.setResetPasswordToken(token);
        user.setResetPasswordExpires(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpires(null);
        userRepository.save(user);
    }

    public UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setVideos(user.getVideos().stream()
                .map(this::convertToVideoDTO)
                .collect(Collectors.toList()));
        return userDTO;
    }

    public VideoDTO convertToVideoDTO(Video video) {
        return new VideoDTO(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                video.getVideoUrl(),
                video.getThumbnailUrl(),
                video.getTimeDuration(),
                video.getUploadTime(),
                null ,
                0,
                false// We don't include user information here to avoid nesting
        );
    }

}
