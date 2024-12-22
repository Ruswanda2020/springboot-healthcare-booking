package com.oneDev.healthcarebooking.service;

import com.oneDev.healthcarebooking.model.request.UserRegisterRequest;
import com.oneDev.healthcarebooking.model.request.UserUpdateRequest;
import com.oneDev.healthcarebooking.model.response.UserResponse;

public interface UserService {
    UserResponse register(UserRegisterRequest userRegisterRequest);
    UserResponse findById(Long userId);
    UserResponse findByUsernameOrEmail(String usernameOrEmail);
    UserResponse updateUser(Long userId, UserUpdateRequest userUpdateRequest);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    void deleteById(Long userId);
}
