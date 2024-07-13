package com.backend.videostreaming.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOTPEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("yashsarode45@gmail.com");
        message.setTo(to);
        message.setSubject("Your OTP for Registration");
        message.setText("Your OTP is: " + otp + ". It will expire in 5 minutes.");
        mailSender.send(message);
    }
    public void sendPasswordResetEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
}