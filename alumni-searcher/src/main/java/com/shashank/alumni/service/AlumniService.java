package com.shashank.alumni.service;
import com.shashank.alumni.dto.AlumniSearchRequest;
import com.shashank.alumni.model.Alumni;
import com.shashank.alumni.repository.AlumniRepository;
import com.shashank.alumni.client.PhantomBusterClient;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AlumniService {

    private final AlumniRepository alumniRepository;
    private final PhantomBusterClient phantomBusterClient;

    public AlumniService(AlumniRepository alumniRepository, PhantomBusterClient phantomBusterClient) {
        this.alumniRepository = alumniRepository;
        this.phantomBusterClient = phantomBusterClient;
    }

    public List<Alumni> searchAndSaveAlumni(AlumniSearchRequest request) {
        // Call PhantomBuster API
        List<Alumni> fetchedProfiles = phantomBusterClient.searchAlumni(request);
        // Save to DB
        return alumniRepository.saveAll(fetchedProfiles);
    }

    public List<Alumni> getAlumniByUniversity(String university) {
        return alumniRepository.findByUniversity(university);
    }

    public List<Alumni> getAlumniByCurrentRole(String currentRole) {
        return alumniRepository.findByCurrentRole(currentRole);
    }

    public List<Alumni> getAlumniByLocation(String location) {
        return alumniRepository.findByLocation(location);
    }

    public Alumni saveAlumni(Alumni alumni) {
        return alumniRepository.save(alumni);
    }

    public List<Alumni> getAllAlumni() {
        return alumniRepository.findAll();
    }
}
