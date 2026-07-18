package com.example.hobbiesmanagment.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
public class WaitlistEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private LearnerProfile learner;

    @ManyToOne
    private MentorProfile mentor;

    @ManyToOne
    private Skill skill;

    @CreationTimestamp
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    private WaitlistStatus status; // (WAITING, NOTIFIED, EXPIRED, ENROLLED)

    private LocalDateTime notificationSentAt;
}