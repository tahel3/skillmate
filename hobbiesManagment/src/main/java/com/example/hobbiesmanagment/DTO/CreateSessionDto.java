package com.example.hobbiesmanagment.DTO;


import com.example.hobbiesmanagment.Entities.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSessionDto {
        private Long mentorId;
        private Long learnerId;
        private Long skillId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private SessionStatus sessionStatus;
}

