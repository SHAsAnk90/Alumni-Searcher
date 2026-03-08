package com.shashank.alumni.repository;
import com.shashank.alumni.model.Alumni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlumniRepository extends JpaRepository<Alumni, Long> {
    List<Alumni> findByUniversity(String university);
    List<Alumni> findByCurrentRole(String currentRole);
    List<Alumni> findByLocation(String location);

}
