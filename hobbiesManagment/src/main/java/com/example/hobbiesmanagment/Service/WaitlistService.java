package com.example.hobbiesmanagment.Service;

import com.example.hobbiesmanagment.DTO.CreateSessionDto;
import com.example.hobbiesmanagment.DTO.CreateWaitlistEntryDto;
import com.example.hobbiesmanagment.DTO.WaitlistResponseDto;
import com.example.hobbiesmanagment.Entities.*;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Repositories.LearnerProfileRepository;
import com.example.hobbiesmanagment.Repositories.MentorProfileRepository;
import com.example.hobbiesmanagment.Repositories.SkillRepository;
import com.example.hobbiesmanagment.Repositories.WaitListRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WaitlistService {

    private final WaitListRepository waitlistRepository;
    private final SkillRepository skillRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final StudentSessionService studentSessionService;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    public WaitlistService(WaitListRepository waitlistRepository, SkillRepository skillRepository,
                           LearnerProfileRepository learnerProfileRepository,
                           MentorProfileRepository mentorProfileRepository,
                           StudentSessionService studentSessionService,
                           ModelMapper modelMapper, NotificationService notificationService) {
        this.waitlistRepository = waitlistRepository;
        this.skillRepository = skillRepository;
        this.learnerProfileRepository = learnerProfileRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.studentSessionService = studentSessionService;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
    }

    public List<WaitlistResponseDto> getWaitlistEntry() {
        return waitlistRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public WaitlistResponseDto getWaitlistEntryById(Long id) {
        WaitlistEntry entry = waitlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found with id: " + id));

        WaitlistResponseDto dto = toResponseDto(entry);
        dto.setQueuePosition(calculateQueuePosition(entry));
        return dto;
    }

    @Transactional
    public WaitlistResponseDto addWaitListEntry(CreateWaitlistEntryDto createDto) {
        if (createDto == null || createDto.getLearnerId() == null || createDto.getMentorId() == null || createDto.getSkillId() == null) {
            throw new IllegalArgumentException("Learner ID, Mentor ID and Skill ID are required");
        }

        Skill skill = skillRepository.findById(createDto.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        LearnerProfile learner = learnerProfileRepository.findByUserId(createDto.getLearnerId());
        if (learner == null) {
            throw new ResourceNotFoundException("Learner profile not found for user id: " + createDto.getLearnerId());
        }

        MentorProfile mentor = mentorProfileRepository.findById(createDto.getMentorId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        boolean alreadyInWaitlist = waitlistRepository.existsByLearnerIdAndSkillIdAndStatus(
                learner.getId(),
                skill.getId(),
                WaitlistStatus.PENDING
        );
        if (alreadyInWaitlist) {
            throw new IllegalStateException("You are already on the waitlist for this lesson");
        }

        WaitlistEntry waitlistEntry = WaitlistEntry.builder()
                .learner(learner)
                .mentor(mentor)
                .skill(skill)
                .status(WaitlistStatus.PENDING)
                .joinedAt(LocalDateTime.now())
                .build();

        WaitlistEntry savedWaitlist = waitlistRepository.save(waitlistEntry);
        return toResponseDto(savedWaitlist);
    }

    @Transactional
    public void deleteWaitListEntry(Long id) {
        WaitlistEntry entry = waitlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot delete, waitlist entry not found with id: " + id));

        waitlistRepository.delete(entry);

        // If the deleted entry was the currently notified one, promote the next person right away
        if (entry.getStatus() == WaitlistStatus.NOTIFIED && entry.getMentor() != null && entry.getSkill() != null) {
            notifyNextInLine(entry.getMentor(), entry.getSkill());
        }
    }

    @Transactional
    public WaitlistResponseDto updateWaitListEntry(WaitlistStatus status, Long id) {
        WaitlistEntry existingEntry = waitlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot update, waitlist entry not found"));
        existingEntry.setStatus(status);
        WaitlistEntry savedEntry = waitlistRepository.save(existingEntry);
        return toResponseDto(savedEntry);
    }

    /**
     * Called by the scheduler to handle entries where the learner didn't respond in time
     */
    @Transactional
    public void processExpirationsAndNotifyNext() {
        LocalDateTime expirationThreshold = LocalDateTime.now().minusHours(2);
        List<WaitlistEntry> expiredEntries = waitlistRepository.findByStatusAndNotificationSentAtBefore(
                WaitlistStatus.NOTIFIED, expirationThreshold);

        for (WaitlistEntry entry : expiredEntries) {
            entry.setStatus(WaitlistStatus.EXPIRED);
            waitlistRepository.save(entry);
            if (entry.getMentor() != null && entry.getSkill() != null) {
                notifyNextInLine(entry.getMentor(), entry.getSkill());
            }
        }
    }

    public List<WaitlistResponseDto> getIncomingRequestsByMentor(Long mentorId) {
        List<WaitlistEntry> entries = waitlistRepository
                .findByMentorIdAndStatus(mentorId, WaitlistStatus.PENDING);
        return entries.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<WaitlistResponseDto> getWaitlistEntriesByLearnerId(Long learnerId) {
        if (learnerId == null) {
            throw new IllegalArgumentException("Learner ID is required");
        }

        LearnerProfile learner = learnerProfileRepository.findByUserId(learnerId);
        if (learner == null) {
            throw new ResourceNotFoundException("Learner profile not found for user id: " + learnerId);
        }

        List<WaitlistEntry> entries = waitlistRepository.findByLearnerId(learner.getId());
        return entries.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private WaitlistResponseDto toResponseDto(WaitlistEntry entry) {
        if (entry == null) {
            return null;
        }

        WaitlistResponseDto dto = new WaitlistResponseDto();
        dto.setId(entry.getId());
        dto.setLearnerId(entry.getLearner() != null ? entry.getLearner().getId() : null);
        dto.setSkillId(entry.getSkill() != null ? entry.getSkill().getId() : null);
        dto.setLearnerName(entry.getLearner() != null && entry.getLearner().getUser() != null
                && entry.getLearner().getUser().getCredential() != null
                ? entry.getLearner().getUser().getCredential().getName()
                : null);
        dto.setMentorName(entry.getMentor() != null && entry.getMentor().getUser() != null
                && entry.getMentor().getUser().getCredential() != null
                ? entry.getMentor().getUser().getCredential().getName()
                : null);
        dto.setSkillName(entry.getSkill() != null ? entry.getSkill().getName() : null);
        dto.setEntryDate(entry.getJoinedAt());
        dto.setWaitlistStatus(entry.getStatus());
        dto.setNotificationSentAt(entry.getNotificationSentAt());
        dto.setQueuePosition(calculateQueuePosition(entry));
        return dto;
    }

    private int calculateQueuePosition(WaitlistEntry entry) {
        if (entry == null || entry.getStatus() != WaitlistStatus.PENDING || entry.getMentor() == null || entry.getSkill() == null) {
            return 0;
        }

        return waitlistRepository.countByMentorIdAndStatusAndJoinedAtBefore(
                entry.getMentor().getId(),
                WaitlistStatus.PENDING,
                entry.getJoinedAt()
        ) + 1;
    }

    /**
     * Finds the next person in line and sends them a notification
     */
    @Transactional
    public void notifyNextInLine(MentorProfile mentor, Skill skill) {
        if (mentor == null || skill == null) {
            return;
        }

        waitlistRepository.findFirstByMentorAndSkillAndStatusOrderByJoinedAtAsc(
                mentor,
                skill,
                WaitlistStatus.PENDING
        ).ifPresent(nextEntry -> {
            nextEntry.setStatus(WaitlistStatus.NOTIFIED);
            nextEntry.setNotificationSentAt(LocalDateTime.now());
            waitlistRepository.save(nextEntry);
            notificationService.sendWaitlistAlert(nextEntry.getLearner(), skill);
        });
    }

    /**
     * Called when a learner clicks "I'm interested" within their allotted response window from the waitlist
     */
    @Transactional
    public void enrollFromWaitlist(Long entryId) {
        WaitlistEntry entry = waitlistRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found"));

        if (entry.getStatus() != WaitlistStatus.NOTIFIED) {
            throw new IllegalStateException("Only NOTIFIED entries can be enrolled. Current status: " + entry.getStatus());
        }

        entry.setStatus(WaitlistStatus.COMPLETED);
        waitlistRepository.save(entry);

        // Find a non-conflicting time slot starting from tomorrow morning
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusHours(1);

        CreateSessionDto newSessionDto = new CreateSessionDto();
        newSessionDto.setLearnerId(entry.getLearner().getUser().getId());
        newSessionDto.setMentorId(entry.getMentor().getId());
        newSessionDto.setSkillId(entry.getSkill().getId());
        newSessionDto.setSessionStatus(SessionStatus.SCHEDULED);
        newSessionDto.setStartTime(startTime);
        newSessionDto.setEndTime(endTime);
        studentSessionService.addStudentSession(newSessionDto);
    }
}