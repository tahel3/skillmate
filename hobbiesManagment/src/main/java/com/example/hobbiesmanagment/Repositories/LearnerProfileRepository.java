package com.example.hobbiesmanagment.Repositories;

import com.example.hobbiesmanagment.Entities.LearnerProfile;
import com.example.hobbiesmanagment.Entities.MentorProfile;
import com.example.hobbiesmanagment.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearnerProfileRepository extends JpaRepository<LearnerProfile, Long> {
    List<LearnerProfile> findByUserCredentialName(String name);
    LearnerProfile findByUserId(Long userId);
    boolean existsByUser(User user);

}
