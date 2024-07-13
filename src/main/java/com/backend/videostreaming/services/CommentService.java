package com.backend.videostreaming.services;

import com.backend.videostreaming.DTOs.CommentDTO;
import com.backend.videostreaming.entities.Comment;
import com.backend.videostreaming.entities.User;
import com.backend.videostreaming.entities.Video;
import com.backend.videostreaming.repositories.CommentRepository;
import com.backend.videostreaming.repositories.UserRepository;
import com.backend.videostreaming.repositories.VideoRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final Logger logger = LoggerFactory.getLogger(CommentService.class);
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public CommentDTO createComment(Long videoId, String content, String userEmail, Long parentCommentId) {
        logger.info("userEmail is {}", userEmail);
        logger.info("videoId is {}", videoId);
        logger.info("content is {}", content);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .video(video)
                .createdAt(LocalDateTime.now())
                .build();

        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }
        logger.info("before saving");
        Comment savedComment = commentRepository.save(comment);
        logger.info("savedComment: {}", savedComment);
        return convertToCommentDTO(savedComment);
    }

    public List<CommentDTO> getCommentsForVideo(Long videoId) {
        List<Comment> topLevelComments = commentRepository.findByVideoIdAndParentCommentIsNullOrderByCreatedAtDesc(videoId);
        return topLevelComments.stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList());
    }

    private CommentDTO convertToCommentDTO(Comment comment) {
        logger.info("Before building");
        CommentDTO.CommentDTOBuilder builder = CommentDTO.builder()
                .commentId(comment.getId())
                .commentText(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replies(new ArrayList<>());

        if (comment.getUser() != null) {
            builder.userId(comment.getUser().getUserId())
                    .userName(comment.getUser().getName())
                    .userEmail(comment.getUser().getEmail());
        }

        if (comment.getVideo() != null) {
            builder.videoId(comment.getVideo().getId());
        }

        if (comment.getParentComment() != null) {
            builder.parentCommentId(comment.getParentComment().getId());
        }

        CommentDTO dto = builder.build();
        logger.info("After building");
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            for (Comment reply : comment.getReplies()) {
                dto.getReplies().add(convertToCommentDTO(reply));
            }
        }
        logger.info("After looping");
        return dto;
    }
}
