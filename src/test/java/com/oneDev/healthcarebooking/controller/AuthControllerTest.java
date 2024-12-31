package com.oneDev.healthcarebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneDev.healthcarebooking.entity.Role;
import com.oneDev.healthcarebooking.entity.User;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.enumaration.RoleType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.UserInfo;
import com.oneDev.healthcarebooking.model.request.AuthRequest;
import com.oneDev.healthcarebooking.model.request.UserRegisterRequest;
import com.oneDev.healthcarebooking.model.response.UserResponse;
import com.oneDev.healthcarebooking.service.AuthService;
import com.oneDev.healthcarebooking.service.JwtService;
import com.oneDev.healthcarebooking.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthService authService;

    private UserRegisterRequest userRegisterRequest;
    private UserResponse userResponse;
    private UserInfo userInfo;
    private AuthRequest authRequest;
    private String mockedToken;

    @BeforeEach
    void setUp() {
        userRegisterRequest = UserRegisterRequest.builder()
                .username("JohnDoe")
                .email("johndoe@example.com")
                .password("Password@123")
                .passwordConfirmation("Password@123")
                .build();

        userResponse = UserResponse.builder()
                .userId(1L)
                .userName("JohnDoe")
                .email("johndoe@example.com")
                .enabled(true)
                .createdAt(null)
                .updatedAt(null)
                .build();

        authRequest = AuthRequest.builder()
                .username("JohnDoe")
                .password("Password@123")
                .build();

        userInfo = UserInfo.builder()
                .user(User.builder()
                        .userId(1L)
                        .username("JohnDoe")
                        .email("johndoe@example.com")
                        .enabled(true)
                        .build())
                .roles(List.of(
                        new Role(1L, RoleType.PATIENT, "Patient Role"),
                        new Role(2L, RoleType.DOCTOR, "Doctor Role")))
                .build();

        mockedToken = "mocked.jwt.token";
    }

    @Test
    void testRegisterUser_whenRequestIsValid() throws Exception {
        when(userService.register(any(UserRegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_id").value(1))
                .andExpect(jsonPath("$.user_name").value("JohnDoe"))
                .andExpect(jsonPath("$.email").value("johndoe@example.com"))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(userService).register(any(UserRegisterRequest.class));
    }

    @Test
    void testRegisterUser_whenRequestIsInvalid() throws Exception {
        UserRegisterRequest invalidRequest = UserRegisterRequest.builder()
                .username("")
                .email("invalid-email-format")
                .password("short")
                .passwordConfirmation("mismatch")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void testLogin_whenCredentialsAreValid() throws Exception {
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(userInfo);
        when(jwtService.generateToken(userInfo)).thenReturn(mockedToken);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockedToken))
                .andExpect(jsonPath("$.user_name").value("JohnDoe"))
                .andExpect(jsonPath("$.email").value("johndoe@example.com"))
                .andExpect(jsonPath("$.roles").isArray())
                .andDo(print());

        verify(authService).authenticate(any(AuthRequest.class));
        verify(jwtService).generateToken(userInfo);
    }

    @Test
    void testLogin_whenCredentialsAreInvalid() throws Exception {
        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new ApplicationException(ExceptionType.FORBIDDEN, "Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.code").value(HttpServletResponse.SC_FORBIDDEN))
                .andDo(print());

        verify(authService).authenticate(any(AuthRequest.class));
    }
}

