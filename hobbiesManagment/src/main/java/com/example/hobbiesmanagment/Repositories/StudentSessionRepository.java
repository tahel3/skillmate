package com.example.hobbiesmanagment.Repositories;

import com.example.hobbiesmanagment.Entities.LearnerProfile;
import com.example.hobbiesmanagment.Entities.MentorProfile;
import com.example.hobbiesmanagment.Entities.SessionStatus;
import com.example.hobbiesmanagment.Entities.StudentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSessionRepository extends JpaRepository<StudentSession, Long> {

    // שליפת כל השיעורים העתידיים של מנטור (לפי טווח תאריכים)
    List<StudentSession> findByMentorAndStartTimeAfterOrderByStartTimeAsc(MentorProfile mentor, LocalDateTime now);

    // שליפת שיעורים של תלמיד שלא שולמו עדיין (בשביל לבדוק חובות)
    @Query("SELECT s FROM StudentSession s WHERE s.learner = :learner AND s.payment.status = 'PENDING'")
    List<StudentSession> findUnpaidSessionsForLearner(@Param("learner") LearnerProfile learner);

    List<StudentSession> findByLearnerId(Long learnerId);
    List<StudentSession> findByMentorId(Long mentorId);

    List<StudentSession> findByMentorIdAndStatus(Long studentId, SessionStatus status);
}