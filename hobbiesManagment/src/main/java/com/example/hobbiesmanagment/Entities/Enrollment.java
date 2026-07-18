package com.example.hobbiesmanagment.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@EqualsAndHashCode()
@ToString()
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity

@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mentor_id", "learner_id", "skill_id"})
})
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "mentor_id", nullable = false)
    private MentorProfile mentor;
    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;
    @ManyToOne
    @JoinColumn(name = "learner_id", nullable = false)
    private LearnerProfile learner;
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    private boolean active;

    @PrePersist
    protected void onCreate() {
        this.enrolledAt = LocalDateTime.now();
        this.active = true;
    }
}