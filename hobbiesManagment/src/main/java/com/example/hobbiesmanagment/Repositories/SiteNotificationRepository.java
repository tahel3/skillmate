package com.example.hobbiesmanagment.Repositories;

import com.example.hobbiesmanagment.Entities.SiteNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SiteNotificationRepository extends JpaRepository<SiteNotification, Long> {
    /*בדיקה האם השם נמצא ממתין ברשימת המתנה*/
    List<SiteNotification> findByUser_IdAndIsReadFalse(Long userId);
}
