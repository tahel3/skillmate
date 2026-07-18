package com.example.hobbiesmanagment.Repositories;

import com.example.hobbiesmanagment.Entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    //חישוב ממוצע דרוג למנטור ספציפי
        @Query("SELECT AVG(r.rating) FROM Review r WHERE r.mentor.id = :mentorId")
        Double getAverageRatingByMentorId(@Param("mentorId") Long mentorId);
    List<Review> findByMentorId(Long mentorId);
}
