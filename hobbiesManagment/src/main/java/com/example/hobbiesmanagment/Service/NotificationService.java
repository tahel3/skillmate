package com.example.hobbiesmanagment.Service;

import com.example.hobbiesmanagment.DTO.SiteNotificationDto;
import com.example.hobbiesmanagment.Entities.SiteNotification;
import com.example.hobbiesmanagment.Entities.LearnerProfile;
import com.example.hobbiesmanagment.Entities.Skill;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Repositories.SiteNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Consolidates all injections above cleanly and avoids NullPointerException
public class NotificationService {

    private final JavaMailSender mailSender;
    private final ModelMapper modelMapper;
    private final SiteNotificationRepository siteNotificationRepository;

    @Transactional
    public void sendWaitlistAlert(LearnerProfile learner, Skill skill) {
        String subject = " !"+skill.getName()+"התפנה מקום בשיעור ";
        String text = String.format("שלום %s, התפנה מקום בשיעור %s. היכנס לאתר תוך שעתיים לאישור.",
                learner.getUser().getCredential().getName(), skill.getName());

        // Save the message to the database for the site's notification panel
        SiteNotification siteNotif = new SiteNotification();
        siteNotif.setUser(learner.getUser());
        siteNotif.setMessage(text);
        siteNotif.setCreatedAt(LocalDateTime.now());
        siteNotif.setRead(false);
        siteNotificationRepository.save(siteNotif);

        // Send the email
        sendRealEmail(learner.getUser().getEmail(), subject, text);
    }

    private void sendRealEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@hobbiesapp.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public List<SiteNotificationDto> getAllNotifications() {
        return siteNotificationRepository.findAll()
                .stream()
                .map(notif -> modelMapper.map(notif, SiteNotificationDto.class))
                .collect(Collectors.toList());
    }

    public SiteNotificationDto getById(Long id) {
        SiteNotification notif = siteNotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return modelMapper.map(notif, SiteNotificationDto.class);
    }

    public List<SiteNotificationDto> getUnreadNotifications(long userId) {
        return siteNotificationRepository.findByUser_IdAndIsReadFalse(userId)
                .stream()
                .map(notif -> modelMapper.map(notif, SiteNotificationDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public SiteNotificationDto create(SiteNotification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        SiteNotification saved = siteNotificationRepository.save(notification);
        return modelMapper.map(saved, SiteNotificationDto.class);
    }

    @Transactional
    public SiteNotificationDto markAsRead(Long id) {
        SiteNotification notif = siteNotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        notif.setRead(true);
        SiteNotification saved = siteNotificationRepository.save(notif);
        return modelMapper.map(saved, SiteNotificationDto.class);
    }

    @Transactional
    public SiteNotificationDto update(Long id, SiteNotificationDto updatedDto) {
        SiteNotification notif = siteNotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        notif.setMessage(updatedDto.getMessage());
        notif.setRead(updatedDto.isRead());

        SiteNotification saved = siteNotificationRepository.save(notif);
        return modelMapper.map(saved, SiteNotificationDto.class);
    }

    @Transactional
    public void delete(Long id) {
        if (!siteNotificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete, notification not found with id: " + id);
        }
        siteNotificationRepository.deleteById(id);
    }
}