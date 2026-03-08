package com.shashank.alumni.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shashank.alumni.dto.AlumniSearchRequest;
import com.shashank.alumni.model.Alumni;
import com.shashank.alumni.service.AlumniService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlumniController.class)
class AlumniControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlumniService alumniService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void searchAlumniReturnsSuccessResponse() throws Exception {
        AlumniSearchRequest request = new AlumniSearchRequest();
        request.setUniversity("University of XYZ");
        request.setDesignation("Software Engineer");

        Alumni mockAlumni = new Alumni();
        mockAlumni.setName("John Doe");
        mockAlumni.setUniversity("University of XYZ");
        mockAlumni.setCurrentRole("Software Engineer");

        List<Alumni> mockData = Collections.singletonList(mockAlumni);

        Mockito.when(alumniService.searchAndSaveAlumni(Mockito.any(AlumniSearchRequest.class)))
                .thenReturn(mockData);

        mockMvc.perform(post("/api/alumni/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("John Doe"))
                .andExpect(jsonPath("$.data[0].university").value("University of XYZ"))
                .andExpect(jsonPath("$.data[0].currentRole").value("Software Engineer"));
    }
}
