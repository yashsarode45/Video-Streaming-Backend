package com.backend.videostreaming.DTOs;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoDetailsDTO {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private String timeDuration;
    private LocalDateTime uploadTime;
    private String userId;
    private int likeCount;
    private boolean isLikedByCurrentUser;
    private String userName;
    private List<CommentDTO> comments;
}
