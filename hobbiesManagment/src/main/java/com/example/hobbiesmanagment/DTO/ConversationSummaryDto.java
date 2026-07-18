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
public class ConversationSummaryDto {
    private Long otherUserId;
    private String otherUserName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}
