package com.example.hobbiesmanagment.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessagePayload {
        private Long id;
        private Long senderId;
        private Long recipientId;
        private String content;
        private LocalDateTime timestamp;
}
