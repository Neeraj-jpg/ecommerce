package com.klu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.klu.dto.AuthResponse;
import com.klu.dto.SigninRequest;
import com.klu.model.Role;
import com.klu.model.User;
import com.klu.repository.UserRepository;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final SigninNotificationService notificationService =
            mock(SigninNotificationService.class);

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder,
                jwtService,
                notificationService
        );
    }

    @Test
    void sendsNotificationAfterSuccessfulSignin() {
        User user = new User(1L, "Neeraj", "neeraj@example.com", "encoded", Role.USER);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("token");

        AuthResponse response = userService.signin(signinRequest(user.getEmail(), "password"));

        assertEquals("token", response.getToken());
        verify(notificationService).sendSignInNotification(user.getName(), user.getEmail());
    }

    @Test
    void doesNotSendNotificationWhenPasswordIsInvalid() {
        User user = new User(1L, "Neeraj", "neeraj@example.com", "encoded", Role.USER);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPassword())).thenReturn(false);

        AuthResponse response =
                userService.signin(signinRequest(user.getEmail(), "wrong-password"));

        assertNull(response);
        verify(notificationService, never())
                .sendSignInNotification(user.getName(), user.getEmail());
    }

    private SigninRequest signinRequest(String email, String password) {
        SigninRequest request = new SigninRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
