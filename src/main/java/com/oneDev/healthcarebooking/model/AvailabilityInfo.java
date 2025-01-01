package com.oneDev.healthcarebooking.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AvailabilityInfo {
    private Long id;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String consultationType;
    private boolean isAvailable;

}
