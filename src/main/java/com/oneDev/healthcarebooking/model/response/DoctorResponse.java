package com.oneDev.healthcarebooking.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.oneDev.healthcarebooking.entity.Doctor;
import com.oneDev.healthcarebooking.model.AvailabilityInfo;
import com.oneDev.healthcarebooking.model.SpecializationInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DoctorResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private Long hospitalId;
    private String hospitalName;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SpecializationInfo> specializations;
    private List<AvailabilityInfo> availabilities;
    
}
