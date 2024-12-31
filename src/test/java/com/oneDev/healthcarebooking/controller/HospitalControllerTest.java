package com.oneDev.healthcarebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneDev.healthcarebooking.enumaration.RoleType;
import com.oneDev.healthcarebooking.model.UserInfo;
import com.oneDev.healthcarebooking.model.response.HospitalResponse;
import com.oneDev.healthcarebooking.service.HospitalService;
import com.oneDev.healthcarebooking.entity.Role;
import com.oneDev.healthcarebooking.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HospitalService hospitalService;

    private HospitalResponse hospitalResponse;
    private UserInfo userInfo;

    @BeforeEach
    void setUp() {
        // Inisialisasi data dummy untuk HospitalResponse
        hospitalResponse = HospitalResponse.builder()
                .id(1L)
                .name("Test Hospital")
                .address("123 Main St")
                .email("test@hospital.com")
                .phone("123456789")
                .description("General Hospital")
                .build();

        // Inisialisasi User dan Role untuk UserInfo
        User user = User.builder()
                .userId(1L)
                .username("patient@example.com")
                .password("password")
                .enabled(true)
                .build();

        Role role = new Role();
        role.setName(RoleType.PATIENT);

        userInfo = UserInfo.builder()
                .user(user)
                .roles(List.of(role))
                .build();

        // Mock SecurityContextHolder
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userInfo, null, userInfo.getAuthorities());
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void testSearchHospitals_withPatientRole() throws Exception {
        // Mock data untuk hasil pencarian
        List<HospitalResponse> hospitalList = List.of(hospitalResponse);
        Page<HospitalResponse> hospitalPage = new PageImpl<>(hospitalList, PageRequest.of(0, 10, Sort.by("name").ascending()), hospitalList.size());

        when(hospitalService.search("name", PageRequest.of(0, 10, Sort.by("name").ascending()))).thenReturn(hospitalPage);

        mockMvc.perform(get("/hospitals")
                        .param("keyword", "name")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Hospital"))
                .andExpect(jsonPath("$.content[0].address").value("123 Main St"))
                .andExpect(jsonPath("$.content[0].email").value("test@hospital.com"))
                .andExpect(jsonPath("$.content[0].phone").value("123456789"))
                .andExpect(jsonPath("$.content[0].description").value("General Hospital"));
    }

    @Test
    void testGetHospitalById_withPatientRole() throws Exception {
        when(hospitalService.get(1L)).thenReturn(hospitalResponse);

        mockMvc.perform(get("/hospitals/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Hospital"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.email").value("test@hospital.com"))
                .andExpect(jsonPath("$.phone").value("123456789"))
                .andExpect(jsonPath("$.description").value("General Hospital"));
    }
}
