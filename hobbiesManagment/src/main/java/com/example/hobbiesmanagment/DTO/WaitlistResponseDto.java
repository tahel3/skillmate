package com.example.hobbiesmanagment.DTO;

import com.example.hobbiesmanagment.Entities.WaitlistStatus;
import lombok.*;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class WaitlistResponseDto {
    private Long id;

    private Long learnerId;
    private Long skillId;
    private String learnerName;
    private String mentorName;
    private String skillName;
    private LocalDateTime entryDate;
    private WaitlistStatus waitlistStatus;
    private LocalDateTime notificationSentAt;
    private int queuePosition;
}
