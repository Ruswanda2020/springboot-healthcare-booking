package com.oneDev.healthcarebooking.model.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.oneDev.healthcarebooking.enumaration.RoleType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrantUserRoleRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Role type is required")
    private RoleType roleType;
}
