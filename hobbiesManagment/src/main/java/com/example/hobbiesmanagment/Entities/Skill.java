package com.example.hobbiesmanagment.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Builder
@ToString
@Table(name = "skills", indexes = {
        @Index(name = "idx_skill_category", columnList = "category")})
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double cost;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    private Level level;
    private String image;
    private String category;

    @ManyToMany(mappedBy = "skills")
    @JsonIgnore
    private Set<MentorProfile> mentors;

    @ManyToMany(mappedBy = "hobbies")
    @JsonIgnore
    private Set<LearnerProfile> learners;

    // רשימת כל השיעורים שמתקיימים עבור הכישור הזה
    @OneToMany(mappedBy = "skill")
    @JsonIgnore
    private Set<StudentSession> sessions;

    // רשימת כל האנשים שממתינים בתור עבור הכישור הזה
    @OneToMany(mappedBy = "skill")
    @JsonIgnore
    private Set<WaitlistEntry> waitlistEntries;
}
