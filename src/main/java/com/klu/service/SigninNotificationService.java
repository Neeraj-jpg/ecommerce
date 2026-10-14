package com.klu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SigninNotificationService {

    private static final Logger logger =
            LoggerFactory.getLogger(SigninNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${smartcart.mail.from}")
    private String mailFrom;

    public SigninNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async("taskExecutor")
    public void sendSignInNotification(String name, String email) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(mailFrom);
        mail.setTo(email);
        mail.setSubject("SmartCart sign-in notification");
        mail.setText("Hello " + name + ",\n\n"
                + "A successful sign-in to your SmartCart account was just detected.\n"
                + "If this wasn't you, please secure your account.\n\n"
                + "SmartCart");

        try {
            mailSender.send(mail);
        } catch (MailException exception) {
            logger.error("Failed to send SmartCart sign-in notification", exception);
        }
    }
}
