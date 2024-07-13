package com.backend.videostreaming.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentRequest {
    private Long videoId;
    private String content;
    private Long parentCommentId; // Optional field
}
