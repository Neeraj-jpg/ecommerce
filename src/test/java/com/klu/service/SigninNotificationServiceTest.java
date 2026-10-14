package com.klu.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

class SigninNotificationServiceTest {

    @Test
    void sendsNotificationToTheSignedInAddress() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        SigninNotificationService service =
                new SigninNotificationService(mailSender);
        ReflectionTestUtils.setField(service, "mailFrom", "shop@example.com");

        service.sendSignInNotification("Neeraj", "neeraj@example.com");

        ArgumentCaptor<SimpleMailMessage> mailCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());

        SimpleMailMessage mail = mailCaptor.getValue();
        assertArrayEquals(new String[] {"neeraj@example.com"}, mail.getTo());
        assertEquals("shop@example.com", mail.getFrom());
        assertEquals("SmartCart sign-in notification", mail.getSubject());
        org.junit.jupiter.api.Assertions.assertTrue(
                mail.getText().contains("successful sign-in"));
    }
}
