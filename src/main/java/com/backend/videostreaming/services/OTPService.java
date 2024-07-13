package com.backend.videostreaming.services;

import com.backend.videostreaming.entities.OTP;
import com.backend.videostreaming.repositories.OTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OTPService {

    @Autowired
    private OTPRepository otpRepository;

    @Autowired
    private EmailService emailService;

    public String generateAndSaveOTP(String email) {
        String otp = generateOTP();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        OTP otpEntity = OTP.builder()
                .email(email)
                .otp(otp)
                .expiresAt(expiryTime)
                .build();

        otpRepository.save(otpEntity);
        emailService.sendOTPEmail(email, otp);

        return otp;
    }

    public boolean validateOTP(String email, String otp) {
        OTP latestOTP = otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new RuntimeException("No OTP found for this email"));

        return latestOTP.getOtp().equals(otp) && LocalDateTime.now().isBefore(latestOTP.getExpiresAt());
    }

    private String generateOTP() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}