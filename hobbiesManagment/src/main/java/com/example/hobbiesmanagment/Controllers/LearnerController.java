package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.LearnerProfileDto;
import com.example.hobbiesmanagment.Entities.LearnerProfile;
import com.example.hobbiesmanagment.Entities.Skill;
import com.example.hobbiesmanagment.Service.LearnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/learners")
public class LearnerController {

    private final LearnerService learnerService;

    @GetMapping
    public ResponseEntity<List<LearnerProfileDto>> getAllLearners() {
        List<LearnerProfileDto> list = learnerService.getAllLearners();
        if (list.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LearnerProfileDto> getLearnerById(@PathVariable Long id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(learnerService.getLearnerById(id));
    }

    @GetMapping("/{id}/favorites")
    public ResponseEntity<List<Skill>> getFavoriteSkills(@PathVariable Long id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(learnerService.getFavoriteSkillsByLearnerId(id));
    }

    @PostMapping("/{learnerId}/favorites/{skillId}")
    public ResponseEntity<Void> addFavoriteSkill(@PathVariable Long learnerId, @PathVariable Long skillId) {
        if (learnerId <= 0 || skillId <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        learnerService.addFavoriteSkill(learnerId, skillId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{learnerId}/favorites/{skillId}")
    public ResponseEntity<Void> removeFavoriteSkill(@PathVariable Long learnerId, @PathVariable Long skillId) {
        if (learnerId <= 0 || skillId <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        learnerService.removeFavoriteSkill(learnerId, skillId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<LearnerProfileDto> addLearner(@RequestBody LearnerProfile learnerProfile) {
        if (learnerProfile == null)
            throw new IllegalArgumentException("Learner data cannot be empty");

        return ResponseEntity.status(HttpStatus.CREATED).body(learnerService.addLearner(learnerProfile));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LearnerProfileDto> updateLearner(@PathVariable Long id, @RequestBody LearnerProfile learnerProfile) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        if (learnerProfile == null)
            throw new IllegalArgumentException("Update data cannot be empty");

        return ResponseEntity.ok(learnerService.updateLearner(id, learnerProfile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLearner(@PathVariable Long id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        learnerService.deleteLearner(id);
        return ResponseEntity.noContent().build();
    }
}