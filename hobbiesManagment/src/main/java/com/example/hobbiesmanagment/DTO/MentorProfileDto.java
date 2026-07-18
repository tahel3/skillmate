package com.example.hobbiesmanagment.DTO;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfileDto {
    private Long mentorId;
    private String fullName;
    private String email;
    private String phone;
    private String bio;
    private double averageRating;
    private int maxStudents;
    private int currentStudents;
    private List<String> skillNames;
    private List<EnrollmentSummaryDto> activeEnrollments;
}