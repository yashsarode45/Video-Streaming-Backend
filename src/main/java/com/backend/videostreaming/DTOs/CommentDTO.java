package com.backend.videostreaming.DTOs;

import lombok.*;


import java.time.LocalDateTime;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {
    private Long commentId;
    private String commentText;
    private String userId;
    private String userName;
    private String userEmail;
    private Long videoId;
    private Long parentCommentId;
    private LocalDateTime createdAt;
    private List<CommentDTO> replies;
}
