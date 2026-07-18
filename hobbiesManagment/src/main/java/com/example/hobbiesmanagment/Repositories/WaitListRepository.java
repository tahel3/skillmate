package com.example.hobbiesmanagment.Repositories;

import com.example.hobbiesmanagment.DTO.WaitlistResponseDto;
import com.example.hobbiesmanagment.Entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitListRepository extends JpaRepository<WaitlistEntry, Long> {
    long countByMentorIdAndStatus(Long mentorId, WaitlistStatus status);
    /**
     * מוצא את כל הרשומות שקיבלו הודעה לפני זמן מסוים ועדיין בסטטוס NOTIFIED.
     * זה משמש את ה-Scheduler כדי למצוא למי פג התוקף.
     */
    List<WaitlistEntry> findByStatusAndNotificationSentAtBefore(WaitlistStatus status, LocalDateTime dateTime);
    /**
     * מוצא את התלמיד הראשון שהצטרף לרשימה עבור מיומנות ספציפית ומחכה (PENDING).
     * המיון לפי JoinedAt Ascending מבטיח שמי שנכנס קודם יקבל עדיפות.
     */
    Optional<WaitlistEntry> findFirstBySkillAndStatusOrderByJoinedAtAsc(Skill skill, WaitlistStatus status);
    Optional<WaitlistEntry> findFirstByMentorAndSkillAndStatusOrderByJoinedAtAsc(MentorProfile mentor, Skill skill, WaitlistStatus status);

        // מציאת כל התלמידים שמחכים לכישור מסוים אצל מנטור מסוים
        List<WaitlistEntry> findByMentorAndSkillAndStatus(MentorProfile mentor, Skill skill, WaitlistStatus status);
        List<WaitlistEntry> findByMentorId(Long mentorId);
        // מציאת כל רשימות ההמתנה של תלמיד מסוים
        List<WaitlistEntry> findByLearnerId(Long learnerId);
        List<WaitlistEntry> findByMentorIdAndStatus(Long id,WaitlistStatus status);
    List<WaitlistEntry> findByMentorIdAndStatusOrderByJoinedAtAsc(Long mentorId, WaitlistStatus status);
        //מציאת מיקום הממתין בתור
        int countBySkillIdAndStatusAndJoinedAtBefore(Long skillId, WaitlistStatus status, LocalDateTime joinedAt);
        int countByMentorIdAndStatusAndJoinedAtBefore(Long mentorId, WaitlistStatus status, LocalDateTime joinedAt);
    boolean existsByLearnerIdAndSkillIdAndStatus(Long learnerId, Long skillId, WaitlistStatus status);
}
