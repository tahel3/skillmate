package com.example.hobbiesmanagment.DTO;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ReviewRequestDTO {
    private Long mentorId;
    private int rating;
    private String comment;
    private Long learnerId;
}
