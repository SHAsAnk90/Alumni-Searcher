package com.shashank.alumni.service;

import com.shashank.alumni.client.PhantomBusterClient;
import com.shashank.alumni.dto.AlumniSearchRequest;
import com.shashank.alumni.model.Alumni;
import com.shashank.alumni.repository.AlumniRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlumniServiceTest {

    @Mock
    private AlumniRepository alumniRepository;

    @Mock
    private PhantomBusterClient phantomBusterClient;

    @InjectMocks
    private AlumniService alumniService;

    private AlumniSearchRequest mockRequest;
    private List<Alumni> mockProfiles;

    @BeforeEach
    void setUp() {
        mockRequest = new AlumniSearchRequest();
        mockRequest.setUniversity("University of XYZ");
        mockRequest.setDesignation("Software Engineer");

        Alumni a1 = new Alumni();
        a1.setName("Alice");
        Alumni a2 = new Alumni();
        a2.setName("Bob");
        mockProfiles = Arrays.asList(a1, a2);
    }

    @Test
    void searchAndSaveAlumni_Success() {
        // Arrange
        when(phantomBusterClient.searchAlumni(any(AlumniSearchRequest.class))).thenReturn(mockProfiles);
        when(alumniRepository.saveAll(anyList())).thenReturn(mockProfiles);

        // Act
        List<Alumni> result = alumniService.searchAndSaveAlumni(mockRequest);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("Bob", result.get(1).getName());
        verify(phantomBusterClient, times(1)).searchAlumni(mockRequest);
        verify(alumniRepository, times(1)).saveAll(mockProfiles);
    }
}
