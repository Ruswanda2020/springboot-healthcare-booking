package com.oneDev.healthcarebooking.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.oneDev.healthcarebooking.entity.Role;
import com.oneDev.healthcarebooking.entity.User;
import com.oneDev.healthcarebooking.enumaration.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserResponse {

    private Long userId;
    private String userName;
    private String email;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RoleType> roles;

    public static UserResponse from(User user, List<Role> roles) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roles.stream()
                        .map(Role::getName).toList())
                .build();
    }

}
