package com.example.hobbiesmanagment.DTO;

import com.example.hobbiesmanagment.Entities.MentorProfile;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDto {
    //הצגת רישום לפעילות מסוימת
    private Long id;
    private String mentorName;
    private String learnerName;
    private LocalDateTime enrolledAt;
    private boolean active;
}
