package com.backend.videostreaming.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String userId;
    private String name;
    private String email;
    private List<VideoDTO> videos;
    private List<LikedVideoDTO> likedVideos;
}