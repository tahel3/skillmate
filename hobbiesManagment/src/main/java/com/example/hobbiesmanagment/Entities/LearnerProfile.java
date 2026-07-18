package com.example.hobbiesmanagment.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
@Entity
@EqualsAndHashCode()
@ToString()
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="learner_Profile")
public class LearnerProfile{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "learner_hobbies",
            joinColumns = @JoinColumn(name = "learner_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> hobbies;
    @ManyToMany
    @JoinTable(
            name = "learner_favorite_skills",
            joinColumns = @JoinColumn(name = "learner_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> favoriteSkills;

    @OneToMany(mappedBy = "learner", cascade = CascadeType.ALL)
    private Set<StudentSession> sessions;
}
