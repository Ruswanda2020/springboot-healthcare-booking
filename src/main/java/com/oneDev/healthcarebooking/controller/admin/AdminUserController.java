package com.oneDev.healthcarebooking.controller.admin;

import com.oneDev.healthcarebooking.model.request.GrantUserRoleRequest;
import com.oneDev.healthcarebooking.model.response.UserResponse;
import com.oneDev.healthcarebooking.service.UserService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin/users")
@RestController
@SecurityRequirement(name = "Bearer")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @PostMapping("/grant")
    public ResponseEntity<UserResponse> grant(@Valid @RequestBody GrantUserRoleRequest request) {
        UserResponse userResponse = userService.grantUserRole(request.getUserId(), request.getRoleType());
        return ResponseEntity.ok(userResponse);
    }
}
