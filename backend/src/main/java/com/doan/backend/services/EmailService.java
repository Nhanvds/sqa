package com.doan.backend.services;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Service
public class EmailService {
    JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String subject, String verificationUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText("Click the link below to verify your account: " + verificationUrl);
        message.setFrom("thangnv24062002@gmail.com");
        mailSender.send(message);
    }
}
