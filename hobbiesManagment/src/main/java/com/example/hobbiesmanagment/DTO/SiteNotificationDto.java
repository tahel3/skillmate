package com.example.hobbiesmanagment.DTO;

import com.example.hobbiesmanagment.Entities.SiteNotification;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteNotificationDto {

    private Long id;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    public static SiteNotificationDto fromEntity(SiteNotification notification) {
        return SiteNotificationDto.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
