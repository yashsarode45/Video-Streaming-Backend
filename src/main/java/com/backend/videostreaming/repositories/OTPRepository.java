package com.backend.videostreaming.repositories;

import com.backend.videostreaming.entities.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTP, Long> {
    Optional<OTP> findTopByEmailOrderByCreatedAtDesc(String email);
}
