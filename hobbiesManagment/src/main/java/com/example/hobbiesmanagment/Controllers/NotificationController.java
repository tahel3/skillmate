package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.SiteNotificationDto;
import com.example.hobbiesmanagment.Entities.SiteNotification;
import com.example.hobbiesmanagment.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<SiteNotificationDto>> getAllNotifications() {
        List<SiteNotificationDto> list = notificationService.getAllNotifications();
        if (list.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteNotificationDto> getNotificationById(@PathVariable Long id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(notificationService.getById(id));
    }

    @GetMapping("/unread/{userId}")
    public ResponseEntity<List<SiteNotificationDto>> getUnreadNotifications(@PathVariable int userId) {
        if (userId <= 0)
            throw new IllegalArgumentException("User ID must be a positive number");

        List<SiteNotificationDto> list = notificationService.getUnreadNotifications(userId);
        if (list.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<SiteNotificationDto> addNotification(@RequestBody SiteNotification siteNotification) {
        if (siteNotification == null)
            throw new IllegalArgumentException("Notification data cannot be empty");

        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(siteNotification));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SiteNotificationDto> updateNotification(@PathVariable Long id, @RequestBody SiteNotificationDto siteNotification) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        if (siteNotification == null)
            throw new IllegalArgumentException("Update data cannot be empty");

        return ResponseEntity.ok(notificationService.update(id, siteNotification));
    }

    @PutMapping("/mark-as-read/{notificationId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        if (notificationId <= 0)
            throw new IllegalArgumentException("Notification ID must be a positive number");

        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}