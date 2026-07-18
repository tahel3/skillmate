package com.example.hobbiesmanagment.Repositories;

import com.example.hobbiesmanagment.Entities.Level;
import com.example.hobbiesmanagment.Entities.Skill;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Set;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    // שליפת הכישורים הכי מבוקשים ברשימת המתנה (לפי גודל הרשימה)
    @Query("SELECT s FROM Skill s JOIN s.waitlistEntries w GROUP BY s.id ORDER BY COUNT(w) DESC")
    List<Skill> findMostPopularSkillsInWaitlist();

    // חיפוש כישורים לפי קטגוריה ורמה
    @Query("SELECT s FROM Skill s WHERE " +
            "(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:category IS NULL OR LOWER(s.category) = LOWER(:category))")
    Page<Skill> findSkillsByCriteria(@Param("name") String name,
                                     @Param("category") String category,
                                     Pageable pageable);
    @Query("SELECT s FROM MentorProfile mp JOIN mp.skills s WHERE mp.id = :mentorId")
    List<Skill> getMentorProfileSkillsByUserId(@Param("mentorId") Long mentorId);
    public Skill findByName(String name);
    public List<Skill>getSkillsByLevel(Level level);
    List<Skill> findByCostAndMentors(Double cost,String mentorName);
    List<Skill> findByCost(Double cost);


}