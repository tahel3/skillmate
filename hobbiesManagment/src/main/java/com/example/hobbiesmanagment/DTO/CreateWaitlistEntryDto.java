package com.example.hobbiesmanagment.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateWaitlistEntryDto {
    private Long learnerId;
    private Long mentorId;
    private Long skillId;
}
