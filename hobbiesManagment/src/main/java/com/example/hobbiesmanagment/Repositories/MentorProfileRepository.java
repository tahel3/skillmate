package com.example.hobbiesmanagment.Repositories;

import com.example.hobbiesmanagment.Entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {

    // מציאת כל המנטורים שמלמדים כישור ספציפי
    List<MentorProfile> findBySkillsContaining(Skill skill);
    // מציאת מנטורים שיש להם מקום פנוי
    List<MentorProfile> findByCurrentStudentsLessThan(int maxStudents);
    List<MentorProfile> findByUserCredentialName(String name);
    List<MentorProfile> findByMaxStudentsGreaterThan(int minStudents);
    boolean existsByUserId(Long userId);
    MentorProfile findByUserId(Long userId);
    MentorProfile findByUserEmail(String email);
    boolean existsByUser(User user);


}