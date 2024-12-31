package com.oneDev.healthcarebooking.service.impl;

import com.oneDev.healthcarebooking.entity.Hospital;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.request.HospitalRequest;
import com.oneDev.healthcarebooking.model.response.HospitalResponse;
import com.oneDev.healthcarebooking.repository.HospitalRepository;
import com.oneDev.healthcarebooking.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class
HospitalServiceImplTest {

    @Mock
    private HospitalRepository hospitalRepository;
    
    @Mock
    private CacheService cacheService;

    @InjectMocks
    private HospitalServiceImpl hospitalService;

    private Hospital hospital;
    private HospitalRequest hospitalRequest;
    private HospitalResponse hospitalResponse;

    @BeforeEach
    void setUp() {
        hospitalRequest = HospitalRequest.builder()
                .name("testHospital")
                .email("test@example.com")
                .phone("08666666")
                .address("testAddress")
                .description("testDescription")
                .build();

        hospital = Hospital.builder()
                .id(1L)
                .name("testHospital")
                .email("test@example.com")
                .phone("08666666")
                .address("testAddress")
                .description("testDescription")
                .build();

        hospitalResponse = HospitalResponse.builder()
                .id(1L)
                .name("testHospital")
                .email("test@example.com")
                .phone("08666666")
                .address("testAddress")
                .description("testDescription")
                .build();
    }

    @Test
    void search_shouldReturnPageOfHospitalResponse() {
        Page<Hospital> hospitalPage = new PageImpl<>(Collections.singletonList(hospital));
        when(hospitalRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(hospitalPage);

        Page<HospitalResponse> result = hospitalService.search("test", Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testHospital", result.getContent().get(0).getName());
        verify(hospitalRepository, times(1))
                .findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void get_shouldReturnHospitalResponseFromCache() {
        when(cacheService.get(anyString(), eq(HospitalResponse.class)))
                .thenReturn(Optional.of(hospitalResponse));

        HospitalResponse result = hospitalService.get(1L);

        assertNotNull(result);
        assertEquals(hospitalResponse.getId(), result.getId());
        verify(cacheService, times(1)).get(anyString(), eq(HospitalResponse.class));
        verify(hospitalRepository, never()).findById(anyLong());
    }

    @Test
    void get_shouldThrowExceptionWhenNotFound() {
        when(cacheService.get(anyString(), eq(HospitalResponse.class))).thenReturn(Optional.empty());
        when(hospitalRepository.findById(anyLong())).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> hospitalService.get(1L));

        assertEquals(ExceptionType.RESOURCE_NOT_FOUND, exception.getType());
        verify(hospitalRepository, times(1)).findById(anyLong());
    }

    @Test
    void update_shouldReturnUpdatedHospitalResponse() {
        when(hospitalRepository.findById(anyLong())).thenReturn(Optional.of(hospital));
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);

        HospitalResponse result = hospitalService.update(1L, hospitalRequest);

        assertNotNull(result);
        assertEquals(hospital.getId(), result.getId());
        assertEquals(hospitalRequest.getName(), result.getName());
        verify(hospitalRepository, times(1)).findById(anyLong());
        verify(hospitalRepository, times(1)).save(any(Hospital.class));
        verify(cacheService, times(1)).put(anyString(), any(HospitalResponse.class));
    }

    @Test
    void create_shouldReturnHospitalResponse() {
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);

        HospitalResponse result = hospitalService.create(hospitalRequest);

        assertNotNull(result);
        assertEquals(hospital.getName(), result.getName());
        verify(hospitalRepository, times(1)).save(any(Hospital.class));
    }

    @Test
    void delete_shouldEvictCacheAndDeleteHospital() {
        when(hospitalRepository.existsById(anyLong())).thenReturn(true);

        hospitalService.delete(1L);

        verify(hospitalRepository, times(1)).existsById(anyLong());
        verify(hospitalRepository, times(1)).deleteById(anyLong());
        verify(cacheService, times(1)).evict(anyString());
    }

    @Test
    void delete_shouldThrowExceptionWhenNotFound() {
        when(hospitalRepository.existsById(anyLong())).thenReturn(false);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> hospitalService.delete(1L));

        assertEquals(ExceptionType.RESOURCE_NOT_FOUND, exception.getType());
        verify(hospitalRepository, times(1)).existsById(anyLong());
        verify(hospitalRepository, never()).deleteById(anyLong());
    }
}
