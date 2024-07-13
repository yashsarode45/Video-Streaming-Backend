package com.backend.videostreaming.controllers;

import com.backend.videostreaming.DTOs.CommentDTO;
import com.backend.videostreaming.DTOs.LikedVideoDTO;
import com.backend.videostreaming.DTOs.UserDTO;
import com.backend.videostreaming.DTOs.VideoDTO;
import com.backend.videostreaming.DTOs.VideoDetailsDTO;
import com.backend.videostreaming.services.CommentService;
import com.backend.videostreaming.services.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VideoController {

    @Autowired
    private VideoService videoService;


    @GetMapping("/allVideos")
    public List<VideoDTO> getVideos() {
        return videoService.getVideoDTOs();
    }

    @PostMapping("/create-video")
    public ResponseEntity<Map<String, Object>> createVideo(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("video") MultipartFile videoFile,
            @RequestParam("thumbnail") MultipartFile thumbnailFile,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (title == null || description == null || videoFile == null || thumbnailFile == null) {
                response.put("success", false);
                response.put("message", "All fields are required");
                return ResponseEntity.badRequest().body(response);
            }

            // The Authentication object represents the token for an authentication request or for an authenticated principal once the request has been processed by the AuthenticationManager.authenticate(Authentication) method.
            // returns the username of the currently authenticated user.
            String userEmail = authentication.getName();
            VideoDTO createdVideoDto = videoService.createVideo(title, description, videoFile, thumbnailFile, userEmail);

            response.put("success", true);
            response.put("message", "Video created successfully");
            response.put("video", createdVideoDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create video");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/likeVideo/{videoId}")
    public ResponseEntity<Map<String, Object>> likeVideo(@PathVariable Long videoId, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            VideoDTO likedVideo = videoService.likeVideo(videoId, authentication.getName());
            response.put("success", true);
            response.put("message", "Video liked successfully");
            response.put("video", likedVideo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/unlikeVideo/{videoId}")
    public ResponseEntity<Map<String, Object>> unlikeVideo(@PathVariable Long videoId, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            VideoDTO unlikedVideo = videoService.unlikeVideo(videoId, authentication.getName());
            response.put("success", true);
            response.put("message", "Video unliked successfully");
            response.put("video", unlikedVideo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/likedVideosForUser")
    public ResponseEntity<Map<String, Object>> getLikedVideos(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<LikedVideoDTO> likedVideos = videoService.getLikedVideos(authentication.getName());
            response.put("success", true);
            response.put("message", "Liked videos retrieved successfully");
            response.put("videos", likedVideos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/videos/{videoId}/likeCount")
    public ResponseEntity<Map<String, Object>> getVideoLikeCount(@PathVariable Long videoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int likeCount = videoService.getVideoLikeCount(videoId);
            response.put("success", true);
            response.put("message", "Likes for a video retrieved successfully");
            response.put("likeCount", likeCount);
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

    }

    @GetMapping("/videoDetails/{videoId}")
    public ResponseEntity<Map<String, Object>> getVideoDetails(@PathVariable Long videoId, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            VideoDetailsDTO videoDetails = videoService.getVideoDetails(videoId, authentication);
            response.put("success", true);
            response.put("message", "Video details retrieved successfully");
            response.put("video", videoDetails);
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred while retrieving video details");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}