package com.example.hobbiesmanagment.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentSummaryDto {
    private Long id;
    private Long learnerId;
    private String learnerName;
    private LocalDateTime enrolledAt;
    private boolean active;
}