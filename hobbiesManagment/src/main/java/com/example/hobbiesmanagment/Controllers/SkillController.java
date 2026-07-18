package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.SkillSearchResultDto;
import com.example.hobbiesmanagment.Entities.Skill;
import com.example.hobbiesmanagment.Service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/skill")
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    public ResponseEntity<Skill> addSkill(@RequestBody Skill skill) {
        if (skill == null)
            throw new IllegalArgumentException("Skill data cannot be empty");

        Skill created = skillService.addSkill(skill);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {
        List<Skill> list = skillService.getAllSkills();
        if (list.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Skill> updateSkill(@PathVariable Long id, @RequestBody Skill skill) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");
        if (skill == null)
            throw new IllegalArgumentException("Update data cannot be empty");

        return ResponseEntity.ok(skillService.editSkill(id, skill));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-mentor/{mentorId}")
    public ResponseEntity<List<Skill>> getSkillsByMentor(@PathVariable Long mentorId) {
        if (mentorId <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        List<Skill> skills = skillService.getMentorSkills(mentorId);
        if (skills.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SkillSearchResultDto>> searchSkills(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SkillSearchResultDto> skillPage = skillService.getSkillsPageAsDto(name, category, page, size);
        return ResponseEntity.ok(skillPage);
    }
}