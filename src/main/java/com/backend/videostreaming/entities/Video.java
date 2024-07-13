package com.backend.videostreaming.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private String timeDuration;
    private LocalDateTime uploadTime;

//    The @ManyToOne annotation specifies a many-to-one relationship between entities.
//    This means that many instances of one entity are related to one instance of another entity.
//    The @JoinColumn annotation specifies the column in the table that will store the foreign key for the relationship.
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VideoLike> likes;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}