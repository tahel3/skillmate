package com.example.hobbiesmanagment.Repositories;

import com.example.hobbiesmanagment.DTO.EnrollmentDto;
import com.example.hobbiesmanagment.Entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
}
