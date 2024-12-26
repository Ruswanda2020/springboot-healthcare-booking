package com.oneDev.healthcarebooking.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.oneDev.healthcarebooking.entity.Hospital;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class HospitalResponse {
    private Long id;
    private String name;
    private String address;
    private String email;
    private String phone;
    private String description;

    public static HospitalResponse fromHospital(Hospital hospital) {
        return HospitalResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .email(hospital.getEmail())
                .phone(hospital.getPhone())
                .description(hospital.getDescription())
                .build();
    }
}
