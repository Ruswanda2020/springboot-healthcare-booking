package com.oneDev.healthcarebooking.service.impl;

import com.oneDev.healthcarebooking.entity.Role;
import com.oneDev.healthcarebooking.entity.User;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.enumaration.RoleType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.UserInfo;
import com.oneDev.healthcarebooking.model.request.AuthRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private AuthRequest authRequest;

    private UserInfo userInfo;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest("username", "password");
        userInfo = new UserInfo(
                User.builder()
                        .username("username")
                        .build(),
                List.of(Role.builder()
                        .name(RoleType.PATIENT)
                .build())
        );
    }

    @Test
    void authenticate_successFulAuthentication_returnUserInfo() {

        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(userInfo, null);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        // Act & Assert
        UserInfo result = authService.authenticate(authRequest);

        // Verifikasi
        assertNotNull(result);
        assertEquals(userInfo.getUsername(), result.getUsername());
    }

    @Test
    void authenticate_failedAuthentication_throwsAuthenticationException() {

        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new ApplicationException(ExceptionType.INVALID_PASSWORD));

        // Act & Assert
        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> authService.authenticate(authRequest)
        );

        // Verifikasi exception tipe dan pesan
        assertEquals(ExceptionType.INVALID_PASSWORD, exception.getType());
        assertEquals(ExceptionType.INVALID_PASSWORD.getMessage(), exception.getMessage());
    }
}