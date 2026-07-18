package com.example.hobbiesmanagment.Service;

import com.example.hobbiesmanagment.DTO.*;
import com.example.hobbiesmanagment.Entities.*;
import com.example.hobbiesmanagment.Exception.DuplicateResourceException;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.engine.spi.Status;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.time.Duration;

@Service
public class MentorService {

    private final WaitListRepository waitlistRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final SkillRepository skillRepository;
    private final ReviewRepository reviewRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final UserRepository userRepository;
    private final StudentSessionRepository studentSessionRepository;
    private final NotificationService notificationService;

    @Autowired
    public MentorService(MentorProfileRepository mentorProfileRepository,
                         UserRepository userRepository,
                         WaitListRepository waitlistRepository,
                         SkillRepository skillRepository,
                         ReviewRepository reviewRepository,
                         LearnerProfileRepository learnerProfileRepository,
                         ModelMapper modelMapper, StudentSessionRepository studentSessionRepository,NotificationService notificationService) {

        this.waitlistRepository = waitlistRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.skillRepository = skillRepository;
        this.learnerProfileRepository = learnerProfileRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.studentSessionRepository = studentSessionRepository;
        this.notificationService=notificationService;
    }

    public List<MentorProfile> getAllMentors() {
        return mentorProfileRepository.findAll();
    }

    public MentorProfile getMentorById(Long id) {
        return mentorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));
    }

    @Transactional
    public MentorSummaryDto getMentorSummaryById(Long id) {
        MentorProfile mentor = mentorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));
        Double avgRating = reviewRepository.getAverageRatingByMentorId(id);
        return MentorSummaryDto.fromEntityWithRating(mentor, avgRating);
    }

    @Transactional
    public MentorSummaryDto addMentor(MentorProfile mentorProfile) {
        User existingUser = userRepository.findById(mentorProfile.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + mentorProfile.getUser().getId()));

        mentorProfile.setUser(existingUser);

        if (mentorProfileRepository.existsByUserId(existingUser.getId())) {
            throw new DuplicateResourceException("Mentor profile already exists for this user");
        }

        MentorProfile savedMentor = mentorProfileRepository.save(mentorProfile);
        return MentorSummaryDto.fromEntity(savedMentor);
    }

    public void deleteMentor(Long id) {
        if (!mentorProfileRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete: Mentor not found");
        }
        mentorProfileRepository.deleteById(id);
    }

    @Transactional
    public MentorSummaryDto updateMentor(Long id, MentorProfile mentorProfile) {
        MentorProfile existingMentorProfile = mentorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));

        existingMentorProfile.setAvailabilities(mentorProfile.getAvailabilities());
        existingMentorProfile.setCurrentStudents(mentorProfile.getCurrentStudents());
        existingMentorProfile.setSkills(mentorProfile.getSkills());
        existingMentorProfile.setEnrollments(mentorProfile.getEnrollments());
        existingMentorProfile.setMaxStudents(mentorProfile.getMaxStudents());
        existingMentorProfile.setUser(mentorProfile.getUser());

        MentorProfile updatedMentor = mentorProfileRepository.save(existingMentorProfile);
        Double avgRating = reviewRepository.getAverageRatingByMentorId(updatedMentor.getId());
        return MentorSummaryDto.fromEntityWithRating(updatedMentor, avgRating);
    }

    public Skill getSkillByNameForMentor(Long mentorId, String skillName) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        return mentor.getSkills().stream()
                .filter(skill -> skill.getName().equalsIgnoreCase(skillName))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found for this mentor"));
    }

    @Transactional
    public void addLearnerToMentor(Long mentorId, Long studentId, String skillName) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        LearnerProfile learner = learnerProfileRepository.findByUserId(studentId);
        Skill skill = getSkillByNameForMentor(mentorId, skillName);
        boolean alreadyEnrolled = mentor.getEnrollments().stream()
                .anyMatch(e -> e.getLearner().getId().equals(learner.getId())
                        && e.getSkill().getId().equals(skill.getId())
                        && e.isActive());

        if (alreadyEnrolled) {
            throw new DuplicateResourceException("Learner is already enrolled with this mentor");
        }

        if (mentor.getCurrentStudents() < mentor.getMaxStudents()) {
            Enrollment enrollment = Enrollment.builder()
                    .mentor(mentor)
                    .learner(learner)
                    .skill(skill)
                    .enrolledAt(LocalDateTime.now())
                    .active(true)
                    .build();

            mentor.setCurrentStudents(mentor.getCurrentStudents() + 1);
            mentor.getEnrollments().add(enrollment);
            mentorProfileRepository.save(mentor);
        } else {
            WaitlistEntry entry = WaitlistEntry.builder()
                    .learner(learner)
                    .mentor(mentor)
                    .skill(skill)
                    .joinedAt(LocalDateTime.now())
                    .status(WaitlistStatus.PENDING)
                    .build();

            waitlistRepository.save(entry);
        }
    }

    public List<MentorProfile> getMentorsWithAvailableSlots(int minSlots) {
        return mentorProfileRepository.findByMaxStudentsGreaterThan(minSlots);
    }

    public List<MentorProfile> getMentorByName(String name) {
        return mentorProfileRepository.findByUserCredentialName(name);
    }

    @Transactional
    public MentorSummaryDto linkExistingSkillToCurrentMentor(Long skillId) {
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        MentorProfile mentor = mentorProfileRepository.findByUserEmail(currentUserEmail);
        if (mentor == null)
            throw new ResourceNotFoundException("Mentor profile not found for current user");
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
        mentor.getSkills().add(skill);
        MentorProfile saved = mentorProfileRepository.save(mentor);
        Double avgRating = reviewRepository.getAverageRatingByMentorId(saved.getId());
        return MentorSummaryDto.fromEntityWithRating(saved, avgRating);
    }

    @Transactional
    public MentorSummaryDto addSkillToCurrentMentor(Skill skill) {
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        MentorProfile mentor = mentorProfileRepository.findByUserEmail(currentUserEmail);
        if (mentor == null)
            throw new ResourceNotFoundException("Mentor profile not found for user: " + currentUserEmail);

        skill.setId(null);
        Skill savedSkill = skillRepository.save(skill);
        mentor.getSkills().add(savedSkill);
        MentorProfile saved = mentorProfileRepository.save(mentor);
        Double avgRating = reviewRepository.getAverageRatingByMentorId(saved.getId());
        return MentorSummaryDto.fromEntityWithRating(saved, avgRating);
    }

    @Transactional
    public void linkExistingSkillToMentor(Long mentorId, Long skillId) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + mentorId));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
        mentor.getSkills().add(skill);
        mentorProfileRepository.save(mentor);
    }

    @Transactional
    public MentorSummaryDto addSkillToUser(Long mentorId, Skill skill) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new EntityNotFoundException("Mentor not found with id: " + mentorId));
        skill.setId(null);
        Skill savedSkill = skillRepository.save(skill);
        mentor.getSkills().add(savedSkill);
        return MentorSummaryDto.fromEntity(mentor);
    }

    @Transactional
    public List<MentorSummaryDto> getFeaturedMentors() {
        List<MentorProfile> mentors = mentorProfileRepository.findAll();
        return mentors.stream().map(mentor -> {
            Double averageRating = reviewRepository.getAverageRatingByMentorId(mentor.getId());
            return MentorSummaryDto.fromEntityWithRating(mentor, averageRating);
        }).collect(Collectors.toList());
    }


    @Transactional
    public MentorSummaryDto updateMentorAvailability(Long mentorId, List<AvailabilityDto> availabilityDtos) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));
        mentor.getAvailabilities().clear();
        for (AvailabilityDto dto : availabilityDtos) {
            Availability availability = Availability.builder()
                    .dayOfWeek(dto.getDayOfWeek())
                    .startTime(dto.getStartTime())
                    .endTime(dto.getEndTime())
                    .mentor(mentor)
                    .build();

            mentor.getAvailabilities().add(availability);
        }

        MentorProfile updatedMentor = mentorProfileRepository.save(mentor);
        return MentorSummaryDto.fromEntity(updatedMentor);
    }

    @Transactional
    public MentorSummaryDto updateMentorCapacity(Long mentorId, int maxStudents) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        int currentStudents = mentor.getCurrentStudents();
        mentor.setMaxStudents(maxStudents);
        MentorProfile updatedMentor = mentorProfileRepository.save(mentor);
        int newSlots = maxStudents - currentStudents;
        if (newSlots > 0) {
            // Fetch waiting learners in order of arrival
            List<WaitlistEntry> pending = waitlistRepository
                    .findByMentorIdAndStatusOrderByJoinedAtAsc(mentorId, WaitlistStatus.PENDING);
            pending.stream()
                    .limit(newSlots)
                    .forEach(entry -> {
                        entry.setStatus(WaitlistStatus.NOTIFIED);
                        entry.setNotificationSentAt(LocalDateTime.now());
                        waitlistRepository.save(entry);
                        notificationService.sendWaitlistAlert(entry.getLearner(), entry.getSkill());
                    });
        }

        Double avgRating = reviewRepository.getAverageRatingByMentorId(updatedMentor.getId());
        return MentorSummaryDto.fromEntityWithRating(updatedMentor, avgRating);
    }
    @Transactional
    public MentorSummaryDto getCurrentMentorProfile() {
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        MentorProfile mentor = mentorProfileRepository.findByUserEmail(currentUserEmail);
        if (mentor == null) {
            // Auto-create a mentor profile for users who registered before this logic existed
            User user = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));
            mentor = mentorProfileRepository.save(MentorProfile.builder()
                    .user(user)
                    .maxStudents(0)
                    .currentStudents(0)
                    .skills(new HashSet<>())
                    .availabilities(new HashSet<>())
                    .enrollments(new HashSet<>())
                    .build());
        }

        long pendingRequests = waitlistRepository.countByMentorIdAndStatus(mentor.getId(), WaitlistStatus.PENDING);

        List<StudentSession> scheduledSessions = studentSessionRepository.findByMentorIdAndStatus(mentor.getId(), SessionStatus.SCHEDULED);
        int activeStudentsCount = (int) scheduledSessions.stream()
                .map(session -> session.getLearner().getId())
                .distinct()
                .count();

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        double hoursThisMonth = scheduledSessions.stream()
                .filter(s -> !s.getStartTime().isBefore(startOfMonth))
                .mapToDouble(s -> Duration.between(s.getStartTime(), s.getEndTime()).toMinutes() / 60.0)
                .sum();

        MentorSummaryDto dto = MentorSummaryDto.fromEntity(mentor);
        dto.setCurrentStudents(activeStudentsCount);
        dto.setActiveStudentsCount(activeStudentsCount);
        dto.setPendingRequestsCount((int) pendingRequests);
        dto.setHoursThisMonth(hoursThisMonth);
        return dto;
    }
    public MentorCalendarDto getMentorCalendarData(Long mentorId) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));
        Set<String> availableDays = mentor.getAvailabilities().stream()
                .map(availability -> availability.getDayOfWeek().toString())
                .collect(Collectors.toSet());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<String> bookedDates = studentSessionRepository.findByMentorIdAndStatus(mentorId, SessionStatus.SCHEDULED)
                .stream()
                .map(session -> session.getStartTime().format(formatter)) // Extract LocalDateTime from the session and format it
                .collect(Collectors.toList());
        return MentorCalendarDto.builder()
                .availableDays(availableDays)
                .bookedDates(bookedDates)
                .build();
    }
    /**
     * Fetches a full mentor profile for use in the React frontend
     */
    @Transactional
    public MentorProfileDto getMentorProfile(Long mentorId) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + mentorId));

        List<EnrollmentSummaryDto> enrollmentsDtoList = mentor.getEnrollments().stream()
                .map(enrollment -> EnrollmentSummaryDto.builder()
                        .id(enrollment.getId())
                        .learnerId(enrollment.getLearner().getId())
                        .learnerName(enrollment.getLearner().getUser().getCredential().getName())
                        .enrolledAt(enrollment.getEnrolledAt())
                        .active(enrollment.isActive())
                        .build())
                .collect(Collectors.toList());

        List<String> skillNames = mentor.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toList());

        return MentorProfileDto.builder()
                .mentorId(mentor.getId())
                .fullName(mentor.getUser().getCredential().getName())
                .email(mentor.getUser().getEmail())
                .phone(mentor.getUser().getPhone())
                .maxStudents(mentor.getMaxStudents())
                .currentStudents(mentor.getCurrentStudents())
                .skillNames(skillNames)
                .activeEnrollments(enrollmentsDtoList)
                .averageRating(0.0)
                .build();
    }

}