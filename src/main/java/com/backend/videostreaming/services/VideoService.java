package com.backend.videostreaming.services;

import com.backend.videostreaming.DTOs.*;
import com.backend.videostreaming.entities.Comment;
import com.backend.videostreaming.entities.User;
import com.backend.videostreaming.entities.Video;
import com.backend.videostreaming.entities.VideoLike;
import com.backend.videostreaming.repositories.UserRepository;
import com.backend.videostreaming.repositories.VideoLikeRepository;
import com.backend.videostreaming.repositories.VideoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private UserService userService;

    @Value("${cloudinary.folder.name}")
    private String folderName;

    public List<VideoDTO> getVideoDTOs() {
        return videoRepository.findAll().stream()
                .map(video -> convertToVideoDTO(video, Optional.empty())) // Pass an empty Optional<User>
                .collect(Collectors.toList());
    }

    @Transactional
    public VideoDTO createVideo(String title, String description, MultipartFile videoFile, MultipartFile thumbnailFile, String userEmail) throws IOException {
        User user = userService.findByEmail(userEmail);

        Map videoUploadResult = cloudinaryService.uploadFile(videoFile, folderName);
        Map thumbnailUploadResult = cloudinaryService.uploadFile(thumbnailFile, folderName);

        Video video = Video.builder()
                .title(title)
                .description(description)
                .videoUrl((String) videoUploadResult.get("secure_url"))
                .thumbnailUrl((String) thumbnailUploadResult.get("secure_url"))
                .timeDuration(videoUploadResult.get("duration") != null ? videoUploadResult.get("duration").toString() : "0")
                .uploadTime(LocalDateTime.now())
                .user(user)
                .build();

        Video createdVideo = videoRepository.save(video);
        return convertToVideoDTO(createdVideo, Optional.of(user));
    }

    @Transactional
    public VideoDTO likeVideo(Long videoId, String userEmail) {
        User user = userService.findByEmail(userEmail);
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Optional<VideoLike> existingLike = videoLikeRepository.findByUserAndVideo(user, video);

        if (existingLike.isPresent()) {
            throw new RuntimeException("Video already liked by user");
        }

        VideoLike videoLike = new VideoLike();
        videoLike.setUser(user);
        videoLike.setVideo(video);

        // Save the VideoLike entity
        VideoLike savedVideoLike = videoLikeRepository.save(videoLike);

        // Update the relationships
        video.getLikes().add(savedVideoLike);
        user.getLikedVideos().add(savedVideoLike);

        return convertToVideoDTO(video, Optional.of(user));
    }

    @Transactional
    public VideoDTO unlikeVideo(Long videoId, String userEmail) {
        User user = userService.findByEmail(userEmail);
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Optional<VideoLike> existingLike = videoLikeRepository.findByUserAndVideo(user, video);

        if (existingLike.isEmpty()) {
            throw new RuntimeException("Video not liked by user");
        }

        VideoLike videoLike = existingLike.get();

        // Remove the relationships
        video.getLikes().remove(videoLike);
        user.getLikedVideos().remove(videoLike);

        // Delete the VideoLike entity
        videoLikeRepository.delete(videoLike);

        return convertToVideoDTO(video, Optional.of(user));
    }


    public List<LikedVideoDTO> getLikedVideos(String userEmail) {
        User user = userService.findByEmail(userEmail);
        return user.getLikedVideos().stream()
                .map(like -> new LikedVideoDTO(convertToVideoDTO(like.getVideo(), Optional.of(user))))
                .collect(Collectors.toList());
    }

    public int getVideoLikeCount(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        return video.getLikes().size();
    }

    public VideoDetailsDTO getVideoDetails(Long videoId, Authentication authentication) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + videoId));

        User currentUser = null;
        if (authentication != null) {
            currentUser = userService.findByEmail(authentication.getName());
        }

        return convertToVideoDetailsDTO(video, currentUser);
    }

    private VideoDTO convertToVideoDTO(Video video, Optional<User> currentUser) {
        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setId(video.getId());
        videoDTO.setTitle(video.getTitle());
        videoDTO.setDescription(video.getDescription());
        videoDTO.setVideoUrl(video.getVideoUrl());
        videoDTO.setThumbnailUrl(video.getThumbnailUrl());
        videoDTO.setTimeDuration(video.getTimeDuration());
        videoDTO.setUploadTime(video.getUploadTime());
        if (video.getUser() != null) {
            videoDTO.setUser(new UserSimpleDTO(video.getUser().getName(), video.getUser().getEmail()));
        }

        videoDTO.setLikeCount(video.getLikes() != null ? video.getLikes().size() : 0);

        // Adjust the logic for setting isLikedByCurrentUser based on whether currentUser is present
        videoDTO.setLikedByCurrentUser(
                currentUser.map(user -> video.getLikes() != null && video.getLikes().stream()
                                .anyMatch(like -> like.getUser().equals(user)))
                        .orElse(false)); // Default to false if currentUserOpt is empty

        return videoDTO;
    }
    private VideoDetailsDTO convertToVideoDetailsDTO(Video video, User currentUser) {
        VideoDetailsDTO dto = new VideoDetailsDTO();
        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setDescription(video.getDescription());
        dto.setVideoUrl(video.getVideoUrl());
        dto.setThumbnailUrl(video.getThumbnailUrl());
        dto.setTimeDuration(video.getTimeDuration());
        dto.setUploadTime(video.getUploadTime());
        if (video.getUser() != null) {
            dto.setUserId(video.getUser().getUserId());
            dto.setUserName(video.getUser().getName());
        }
        dto.setLikeCount(video.getLikes() != null ? video.getLikes().size() : 0);
        dto.setLikedByCurrentUser(currentUser != null && video.getLikes() != null && video.getLikes().stream()
                .anyMatch(like -> like.getUser().equals(currentUser)));

        List<CommentDTO> commentDTOs = video.getComments() != null ? video.getComments().stream()
                .filter(comment -> comment.getParentComment() == null)
                .map(comment -> convertToCommentDTO(comment, new HashSet<>()))
                .collect(Collectors.toList()) : new ArrayList<>();

        dto.setComments(commentDTOs);

        return dto;
    }

    private CommentDTO convertToCommentDTO(Comment comment, Set<Long> processedComments) {
        if (comment == null  || processedComments.contains(comment.getId())) {
            return null; // Avoid circular references
        }
        processedComments.add(comment.getId());

        CommentDTO dto = new CommentDTO();
        dto.setCommentId(comment.getId());
        dto.setCommentText(comment.getContent());
        if (comment.getUser() != null) {
            dto.setUserId(comment.getUser().getUserId());
            dto.setUserName(comment.getUser().getName());
            dto.setUserEmail(comment.getUser().getEmail());
        }

        if (comment.getVideo() != null) {
            dto.setVideoId(comment.getVideo().getId());
        }
        dto.setParentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null);
        dto.setCreatedAt(comment.getCreatedAt());

        List<CommentDTO> replies = comment.getReplies() != null ? comment.getReplies().stream()
                .map(reply -> convertToCommentDTO(reply, new HashSet<>(processedComments)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()) : new ArrayList<>();

        dto.setReplies(replies);

        return dto;
    }
}