package com.example.hobbiesmanagment.DTO;

import com.example.hobbiesmanagment.Entities.SessionStatus;
import com.example.hobbiesmanagment.Entities.StudentSession;
import lombok.*;

import java.time.LocalDateTime;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDto {
    private Long id;
    private Long mentorId;
    private String mentorName;
    private String mentorEmail;
    private String mentorPhone;
    private String skillName;
    private String skillImage;
    private String skillDescription;
    private String learnerName;
    private String learnerEmail;
    private String learnerPhone;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
private SessionStatus sessionStatus;
    public static SessionDto fromEntity(StudentSession studentSession) {
        return SessionDto.builder()
                .id(studentSession.getId())
                .mentorId(studentSession.getMentor().getId())
                .mentorName(studentSession.getMentor().getUser().getCredential().getName())
                .mentorEmail(studentSession.getMentor().getUser().getEmail())
                .mentorPhone(studentSession.getMentor().getUser().getPhone())
                .skillName(studentSession.getSkill().getName())
                .skillImage(studentSession.getSkill().getImage())
                .skillDescription(studentSession.getSkill().getDescription())
                .learnerName(studentSession.getLearner().getUser().getCredential().getName())
                .learnerEmail(studentSession.getLearner().getUser().getEmail())
                .learnerPhone(studentSession.getLearner().getUser().getPhone())
                .startTime(studentSession.getStartTime())
                .endTime(studentSession.getEndTime()).sessionStatus(studentSession.getStatus())
                .build();
    }
}