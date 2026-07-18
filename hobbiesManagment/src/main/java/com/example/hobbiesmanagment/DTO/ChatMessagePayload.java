package com.example.hobbiesmanagment.DTO;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessagePayload {
        private Long senderId;
        private Long recipientId;
        private String content;
}
