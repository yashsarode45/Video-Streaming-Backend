package com.backend.videostreaming.repositories;

import com.backend.videostreaming.entities.User;
import com.backend.videostreaming.entities.Video;
import com.backend.videostreaming.entities.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {
    Optional<VideoLike> findByUserAndVideo(User user, Video video);
}
