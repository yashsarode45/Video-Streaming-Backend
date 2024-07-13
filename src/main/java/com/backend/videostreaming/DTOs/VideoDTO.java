package com.backend.videostreaming.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private String timeDuration;
    private LocalDateTime uploadTime;
    private UserSimpleDTO user;
    private int likeCount;
    private boolean isLikedByCurrentUser;
}
