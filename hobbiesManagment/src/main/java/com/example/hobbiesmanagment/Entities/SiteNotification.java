package com.example.hobbiesmanagment.Entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "siteNotification")//התראות למשתמש
public class SiteNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String message;
    private LocalDateTime createdAt;
    private boolean isRead = false;
}