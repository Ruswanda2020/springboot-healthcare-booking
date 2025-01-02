package com.oneDev.healthcarebooking.controller.admin;

import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.UserInfo;
import com.oneDev.healthcarebooking.model.request.GrantUserRoleRequest;
import com.oneDev.healthcarebooking.model.response.UserResponse;
import com.oneDev.healthcarebooking.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RequiredArgsConstructor
@RequestMapping("/admin/users")
@RestController
@SecurityRequirement(name = "Bearer")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @PostMapping("/grant")
    public ResponseEntity<UserResponse> grant(@Valid @RequestBody GrantUserRoleRequest request) {
        System.out.println("Received userId: " + request.getUserId());
        System.out.println("Received roleType: " + request.getRoleType());
        UserResponse userResponse = userService.grantUserRole(request.getUserId(), request.getRoleType());
        return ResponseEntity.ok(userResponse);
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") Long userId){
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }
}
