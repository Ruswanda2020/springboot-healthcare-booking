package com.oneDev.healthcarebooking.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SpecializationInfo {
    private Long specializationId;
    private String specializationName;
    private BigDecimal baseFee;
    private BigDecimal hospitalFee;
    private String consultationType;
}
