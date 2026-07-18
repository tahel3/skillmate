package com.example.hobbiesmanagment.DTO;

import com.example.hobbiesmanagment.Entities.Skill;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillSearchResultDto {
    private Long id;
    private String name;
    private double cost;
    private String description;
    private String level;
    private String image;
    private String category;
    private Long mentorId; // ה-ID של המנטור הראשון שמלמד את הכישור הזה

    public static SkillSearchResultDto fromSkill(Skill skill) {
        Long mentorId = (skill.getMentors() != null && !skill.getMentors().isEmpty())
                ? skill.getMentors().iterator().next().getId()
                : null;

        return SkillSearchResultDto.builder()
                .id(skill.getId())
                .name(skill.getName())
                .cost(skill.getCost())
                .description(skill.getDescription())
                .level(skill.getLevel() != null ? skill.getLevel().name() : null)
                .image(skill.getImage())
                .category(skill.getCategory())
                .mentorId(mentorId)
                .build();
    }
}
