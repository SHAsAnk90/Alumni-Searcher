package com.shashank.alumni.controller;

import com.shashank.alumni.dto.AlumniSearchRequest;
import com.shashank.alumni.dto.ApiResponse;
import com.shashank.alumni.model.Alumni;
import com.shashank.alumni.service.AlumniService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alumni")
public class AlumniController {

    private final AlumniService alumniService;

    public AlumniController(AlumniService alumniService) {
        this.alumniService = alumniService;
    }

    @PostMapping("/search")
    public ApiResponse<Alumni> searchAlumni(@RequestBody AlumniSearchRequest request) {
        List<Alumni> data = alumniService.searchAndSaveAlumni(request);
        return new ApiResponse<>("success", data);
    }

    @GetMapping("/all")
    public ApiResponse<Alumni> getAllAlumni() {
        List<Alumni> data = alumniService.getAllAlumni();
        return new ApiResponse<>("success", data);
    }

    @GetMapping("/university/{university}")
    public ApiResponse<Alumni> getAlumniByUniversity(@PathVariable String university) {
        List<Alumni> data = alumniService.getAlumniByUniversity(university);
        return new ApiResponse<>("success", data);
    }

    @GetMapping("/role/{role}")
    public ApiResponse<Alumni> getAlumniByRole(@PathVariable String role) {
        List<Alumni> data = alumniService.getAlumniByCurrentRole(role);
        return new ApiResponse<>("success", data);
    }

    @GetMapping("/location/{location}")
    public ApiResponse<Alumni> getAlumniByLocation(@PathVariable String location) {
        List<Alumni> data = alumniService.getAlumniByLocation(location);
        return new ApiResponse<>("success", data);
    }

    @PostMapping
    public ApiResponse<Alumni> saveAlumni(@RequestBody Alumni alumni) {
        Alumni saved = alumniService.saveAlumni(alumni);
        return new ApiResponse<>("success", List.of(saved));
    }
}
