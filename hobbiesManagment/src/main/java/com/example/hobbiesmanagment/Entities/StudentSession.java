package com.example.hobbiesmanagment.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Entity
@Table(name = "sessions")
public class StudentSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "learner_id", nullable = false)
    private LearnerProfile learner;

    @ManyToOne
    @JoinColumn(name = "mentor_id", nullable = false)
    private MentorProfile mentor;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @OneToOne(cascade = CascadeType.ALL) // אם מוחקים שיעור, אולי נרצה לנהל את התיעוד של התשלום
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    private Payment payment;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private SessionStatus status; // (SCHEDULED, COMPLETED, CANCELED)
}