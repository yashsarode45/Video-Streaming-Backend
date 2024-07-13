package com.backend.videostreaming.repositories;

import com.backend.videostreaming.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByVideoIdAndParentCommentIsNullOrderByCreatedAtDesc(Long videoId);

}
