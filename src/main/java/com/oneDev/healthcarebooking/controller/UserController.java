package com.oneDev.healthcarebooking.controller;

import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.UserInfo;
import com.oneDev.healthcarebooking.model.request.UserUpdateRequest;
import com.oneDev.healthcarebooking.model.response.UserResponse;
import com.oneDev.healthcarebooking.service.UserService;
import com.oneDev.healthcarebooking.utils.UserInfoHelper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@SecurityRequirement(name = "Bearer")
public class UserController {

    private final UserService userService;
    private final UserInfoHelper userInfoHelper;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(){
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        UserResponse response = UserResponse.from(userInfo.getUser(), userInfo.getRoles());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable("userId") Long userId,
                                                   @Valid @RequestBody UserUpdateRequest request){

        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();

        if (!Objects.equals(userInfo.getUser().getUserId(), userId)) {
            throw new ApplicationException(ExceptionType.FORBIDDEN, "user "+ userInfo.getUsername() + " is not allowed to update");
        }

        UserResponse updateUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(updateUser);
    }

}
