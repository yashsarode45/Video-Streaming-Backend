package com.backend.videostreaming.controllers;

import com.backend.videostreaming.DTOs.CommentDTO;
import com.backend.videostreaming.models.CommentRequest;
import com.backend.videostreaming.services.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    @Autowired
    private CommentService commentService;

    @PostMapping("/createComment")
    public ResponseEntity<Map<String, Object>> createComment(
            @RequestBody CommentRequest commentRequest,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Received comment request: videoId={}, content={}, parentCommentId={}",
                    commentRequest.getVideoId(), commentRequest.getContent(), commentRequest.getParentCommentId());

            CommentDTO createdComment = commentService.createComment(commentRequest.getVideoId(),
                    commentRequest.getContent(),
                    authentication.getName(),
                   commentRequest.getParentCommentId());
            response.put("success", true);
            response.put("message", "Comment created successfully");
            response.put("comment", createdComment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/video/{videoId}/getAllComments")
    public ResponseEntity<Map<String, Object>> getCommentsForVideo(@PathVariable Long videoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CommentDTO> comments = commentService.getCommentsForVideo(videoId);
            response.put("success", true);
            response.put("message", "Comments retrieved successfully");
            response.put("comments", comments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
