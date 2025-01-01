package com.oneDev.healthcarebooking.model.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DoctorRegistrationRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long hospitalId;

    @Size(max = 1000)
    private String bio;

    @Size(max = 1000)
    private String name;

    @NotNull
    private List<DoctorSpecializationRequest> specializations;
}
