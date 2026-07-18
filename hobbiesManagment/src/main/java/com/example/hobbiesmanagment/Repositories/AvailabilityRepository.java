package com.example.hobbiesmanagment.Repositories;

import com.example.hobbiesmanagment.Entities.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
}
