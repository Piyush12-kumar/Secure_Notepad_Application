package com.example.notepadapp.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    JavaMailSender javaMailSender;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("jammu4337@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");
        message.setText(resetLink);

        javaMailSender.send(message);
    }

}
