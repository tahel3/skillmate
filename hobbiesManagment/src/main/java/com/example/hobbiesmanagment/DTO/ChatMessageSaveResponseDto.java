package com.example.hobbiesmanagment.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageSaveResponseDto{

    private Long senderId;
    private Long recipientId;
    private String content;
    private Long id;
    private LocalDateTime timestamp;
}
