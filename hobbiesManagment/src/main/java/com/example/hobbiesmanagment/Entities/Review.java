package com.example.hobbiesmanagment.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private LearnerProfile reviewer;

    @ManyToOne
    private MentorProfile mentor;

    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}