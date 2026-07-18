package com.example.hobbiesmanagment.DTO;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEnrollmentDto {
    private Long mentorId;
    private Long learnerId;
}
