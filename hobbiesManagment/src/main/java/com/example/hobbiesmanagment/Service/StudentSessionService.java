package com.example.hobbiesmanagment.Service;

import com.example.hobbiesmanagment.DTO.CreateSessionDto;
import com.example.hobbiesmanagment.DTO.SessionDto;
import com.example.hobbiesmanagment.Entities.*;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Repositories.LearnerProfileRepository;
import com.example.hobbiesmanagment.Repositories.MentorProfileRepository;
import com.example.hobbiesmanagment.Repositories.SkillRepository;
import com.example.hobbiesmanagment.Repositories.StudentSessionRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentSessionService {

    private final StudentSessionRepository studentSessionRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final SkillRepository skillRepository;
    private final ModelMapper modelMapper;
    private final WaitlistService waitlistService;

    @Autowired
    public StudentSessionService(StudentSessionRepository studentSessionRepository,
                                 MentorProfileRepository mentorProfileRepository,
                                 LearnerProfileRepository learnerProfileRepository,
                                 SkillRepository skillRepository,
                                 ModelMapper modelMapper,
                                 @Lazy WaitlistService waitlistService) {
        this.studentSessionRepository = studentSessionRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.learnerProfileRepository = learnerProfileRepository;
        this.skillRepository = skillRepository;
        this.modelMapper = modelMapper;
        this.waitlistService = waitlistService;
    }

    public List<SessionDto> getAllStudentSessions() {
        return studentSessionRepository.findAll()
                .stream()
                .map(SessionDto::fromEntity)
                .collect(Collectors.toList());
    }

    public SessionDto getStudentSessionById(long id) {
        return studentSessionRepository.findById(id)
                .map(SessionDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Student Session not found with id: " + id));
    }

    @Transactional
    public SessionDto addStudentSession(CreateSessionDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Session data is required");
        }
        if (dto.getMentorId() == null || dto.getLearnerId() == null || dto.getSkillId() == null) {
            throw new IllegalArgumentException("Mentor, learner, and skill IDs are required");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        MentorProfile mentor = mentorProfileRepository.findById(dto.getMentorId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + dto.getMentorId()));

        LearnerProfile learner = learnerProfileRepository.findByUserId(dto.getLearnerId());
        if (learner == null) {
            throw new ResourceNotFoundException("Learner profile not found for user id: " + dto.getLearnerId());
        }

        // Check for conflicts with the same learner's existing sessions
        List<StudentSession> learnerSessions = studentSessionRepository.findByLearnerId(learner.getId())
                .stream()
                .filter(s -> s.getStatus() == SessionStatus.SCHEDULED)
                .collect(Collectors.toList());

        boolean alreadyRegisteredSameSlot = learnerSessions.stream()
                .anyMatch(s -> s.getMentor().getId().equals(mentor.getId())
                        && s.getStartTime().equals(dto.getStartTime())
                        && s.getEndTime().equals(dto.getEndTime()));
        if (alreadyRegisteredSameSlot) {
            throw new IllegalArgumentException("You have already registered for this lesson");
        }

        boolean hasLearnerTimeConflict = learnerSessions.stream()
                .anyMatch(s -> dto.getStartTime().isBefore(s.getEndTime())
                        && dto.getEndTime().isAfter(s.getStartTime()));
        if (hasLearnerTimeConflict) {
            throw new IllegalArgumentException("You already have another lesson scheduled for this time");
        }

        // Check for conflicts with the same mentor's existing sessions
        List<StudentSession> mentorSessions = studentSessionRepository.findByMentorId(mentor.getId())
                .stream()
                .filter(s -> s.getStatus() == SessionStatus.SCHEDULED)
                .collect(Collectors.toList());

        boolean hasMentorTimeConflict = mentorSessions.stream()
                .anyMatch(s -> dto.getStartTime().isBefore(s.getEndTime())
                        && dto.getEndTime().isAfter(s.getStartTime()));
        if (hasMentorTimeConflict) {
            throw new IllegalArgumentException("This mentor is already booked for the requested time");
        }

        int activeStudentsCount = (int) mentorSessions.stream()
                .map(session -> session.getLearner().getId())
                .distinct()
                .count();

        boolean learnerAlreadyActiveWithMentor = mentorSessions.stream()
                .anyMatch(session -> session.getLearner().getId().equals(learner.getId())
                        && session.getStatus() == SessionStatus.SCHEDULED);

        if (mentor.getMaxStudents() > 0
                && activeStudentsCount >= mentor.getMaxStudents()
                && !learnerAlreadyActiveWithMentor) {
            throw new IllegalStateException("This mentor has reached the maximum number of active students");
        }

        Skill skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + dto.getSkillId()));

        StudentSession session = StudentSession.builder()
                .mentor(mentor)
                .learner(learner)
                .skill(skill)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(dto.getSessionStatus() != null ? dto.getSessionStatus() : SessionStatus.SCHEDULED)
                .build();

        StudentSession savedSession = studentSessionRepository.save(session);

        int updatedActiveCount = (int) studentSessionRepository.findByMentorIdAndStatus(mentor.getId(), SessionStatus.SCHEDULED)
                .stream()
                .map(s -> s.getLearner().getId())
                .distinct()
                .count();
        mentor.setCurrentStudents(updatedActiveCount);
        mentorProfileRepository.save(mentor);

        return SessionDto.fromEntity(savedSession);
    }

    @Transactional
    public SessionDto updateStudentSession(Long id, CreateSessionDto dto) {
        StudentSession existingSession = studentSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        if (dto.getMentorId() != null) {
            MentorProfile mentor = mentorProfileRepository.findById(dto.getMentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));
            existingSession.setMentor(mentor);
        }

        existingSession.setLearner(learnerProfileRepository.findByUserId(dto.getLearnerId()));
        existingSession.setStartTime(dto.getStartTime());
        existingSession.setEndTime(dto.getEndTime());

        StudentSession savedSession = studentSessionRepository.save(existingSession);
        return SessionDto.fromEntity(savedSession);
    }

    @Transactional
    public void deleteStudentSession(long id) {
        StudentSession session = studentSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot delete, session not found with id: " + id));

        MentorProfile mentor = session.getMentor();
        LearnerProfile learner = session.getLearner();
        Skill skill = session.getSkill();

        studentSessionRepository.deleteById(id);

        if (mentor != null) {
            int updatedActiveCount = (int) studentSessionRepository.findByMentorIdAndStatus(mentor.getId(), SessionStatus.SCHEDULED)
                    .stream()
                    .map(s -> s.getLearner().getId())
                    .distinct()
                    .count();
            mentor.setCurrentStudents(updatedActiveCount);
            mentorProfileRepository.save(mentor);

            if (learner != null && skill != null && !hasAnyScheduledSessionForLearnerWithMentor(learner.getId(), mentor.getId())) {
                waitlistService.notifyNextInLine(mentor, skill);
            }
        }
    }

    private boolean hasAnyScheduledSessionForLearnerWithMentor(Long learnerId, Long mentorId) {
        return studentSessionRepository.findByLearnerId(learnerId).stream()
                .anyMatch(s -> s.getMentor() != null
                        && s.getMentor().getId().equals(mentorId)
                        && s.getStatus() == SessionStatus.SCHEDULED);
    }

    // Get sessions for a specific learner
    public List<SessionDto> getSessionsByLearnerId(long userId) {
        LearnerProfile learner = learnerProfileRepository.findByUserId(userId);
        if (learner == null) {
            return new ArrayList<>();
        }
        List<StudentSession> sessions = studentSessionRepository.findByLearnerId(learner.getId());
        if (sessions == null || sessions.isEmpty()) {
            return new ArrayList<>();
        }
        return sessions.stream()
                .map(SessionDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SessionDto> getSessionsByMentorId(Long userId) {
        MentorProfile mentor = mentorProfileRepository.findByUserId(userId);
        if (mentor == null) {
            throw new ResourceNotFoundException("Mentor not found with ID: " + userId);
        }

        return studentSessionRepository.findByMentorId(mentor.getId())
                .stream()
                .map(SessionDto::fromEntity)
                .collect(Collectors.toList());
    }
}