package com.example.hobbiesmanagment.DTO;

import com.example.hobbiesmanagment.Entities.MentorProfile;
import com.example.hobbiesmanagment.Entities.Skill;
import lombok.*;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MentorSummaryDto {
    private Long mentorId;
    private String fullName;
    private String city;
    private double averageRating;
    private int maxStudents;
    private int currentStudents;
    private List<String> skillNames;
    private List<Long> skillIds;
    private List<String> imageSkills;
    private List<String> skillCategories;
    private List<AvailabilityDto> availabilities;
    private List<Double> skillCosts;
    private List<String> skillLevels;
    private int activeStudentsCount;
    private int pendingRequestsCount;
    private double hoursThisMonth;
    public static MentorSummaryDto fromEntity(MentorProfile mentor) {
        List<String> names = mentor.getSkills().stream().map(Skill::getName).collect(Collectors.toList());
        List<Long> ids = mentor.getSkills().stream().map(Skill::getId).collect(Collectors.toList());
        List<Double> costs = mentor.getSkills().stream().map(Skill::getCost).collect(Collectors.toList());
        List<String> images = mentor.getSkills().stream().map(Skill::getImage).collect(Collectors.toList());
        List<String> categories = mentor.getSkills().stream().map(Skill::getCategory).collect(Collectors.toList());
        List<String> levels = mentor.getSkills().stream()
                .map(s -> s.getLevel() != null ? s.getLevel().name() : null)
                .collect(Collectors.toList());
        List<AvailabilityDto> availabilities = mentor.getAvailabilities() == null ? List.of() :
                mentor.getAvailabilities().stream()
                        .map(a -> new AvailabilityDto(a.getId(), a.getDayOfWeek(), a.getStartTime(), a.getEndTime()))
                        .collect(Collectors.toList());
        return MentorSummaryDto.builder()
                .mentorId(mentor.getId())
                .fullName(mentor.getUser().getCredential().getName())
                .city(mentor.getUser().getCity())
                .skillNames(names.isEmpty() ? List.of() : names)
                .skillIds(ids)
                .skillCosts(costs)
                .imageSkills(images)
                .skillCategories(categories)
                .skillLevels(levels)
                .availabilities(availabilities)
                .maxStudents(mentor.getMaxStudents())
                .build();
    }

    public static MentorSummaryDto fromEntityWithRating(MentorProfile mentor, Double avgRating) {
        List<String> names = mentor.getSkills().stream().map(Skill::getName).collect(Collectors.toList());
        List<Long> ids = mentor.getSkills().stream().map(Skill::getId).collect(Collectors.toList());
        List<String> images = mentor.getSkills().stream().map(Skill::getImage).collect(Collectors.toList());
        List<String> categories = mentor.getSkills().stream().map(Skill::getCategory).collect(Collectors.toList());
        List<Double> costs = mentor.getSkills().stream().map(Skill::getCost).collect(Collectors.toList());
        List<String> levels = mentor.getSkills().stream()
                .map(s -> s.getLevel() != null ? s.getLevel().name() : null)
                .collect(Collectors.toList());
        List<AvailabilityDto> availabilities = mentor.getAvailabilities() == null ? List.of() :
                mentor.getAvailabilities().stream()
                        .map(a -> new AvailabilityDto(a.getId(), a.getDayOfWeek(), a.getStartTime(), a.getEndTime()))
                        .collect(Collectors.toList());
        return MentorSummaryDto.builder()
                .mentorId(mentor.getId())
                .fullName(mentor.getUser().getCredential().getName())
                .city(mentor.getUser().getCity())
                .skillNames(names.isEmpty() ? List.of() : names)
                .skillIds(ids)
                .skillCosts(costs)
                .imageSkills(images)
                .skillCategories(categories)
                .skillLevels(levels)
                .availabilities(availabilities)
                .maxStudents(mentor.getMaxStudents())
                .currentStudents(mentor.getCurrentStudents())
                .averageRating(avgRating != null ? avgRating : 0.0)
                .build();
    }
}
