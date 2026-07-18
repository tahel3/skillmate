package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.AvailabilityDto;
import com.example.hobbiesmanagment.DTO.MentorCalendarDto;
import com.example.hobbiesmanagment.DTO.MentorSummaryDto;
import com.example.hobbiesmanagment.Entities.MentorProfile;
import com.example.hobbiesmanagment.Entities.Skill;
import com.example.hobbiesmanagment.Service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentors")
public class MentorController {

    private final MentorService mentorService;

    @GetMapping
    public ResponseEntity<List<MentorSummaryDto>> getAllMentors() {
        return ResponseEntity.ok(mentorService.getFeaturedMentors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentorSummaryDto> getMentorById(@PathVariable Long id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(mentorService.getMentorSummaryById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<MentorSummaryDto> getMyProfile() {
        return ResponseEntity.ok(mentorService.getCurrentMentorProfile());
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<Skill> getMentorSkillByName(@PathVariable Long id, @RequestParam String skillName) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        if (skillName == null || skillName.isBlank())
            throw new IllegalArgumentException("Skill name cannot be empty");

        return ResponseEntity.ok(mentorService.getSkillByNameForMentor(id, skillName));
    }

    @GetMapping("/available/{slot}")
    public ResponseEntity<List<MentorProfile>> getMentors(@PathVariable int slot) {
        if (slot < 0)
            throw new IllegalArgumentException("Number of slots cannot be negative");

        List<MentorProfile> list = mentorService.getMentorsWithAvailableSlots(slot);
        if (list.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/available-dates")
    public ResponseEntity<MentorCalendarDto> getMentorAvailabilityForCalendar(@PathVariable Long id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(mentorService.getMentorCalendarData(id));
    }

    @PostMapping
    public ResponseEntity<MentorSummaryDto> addMentor(@RequestBody MentorProfile mentorProfile) {
        if (mentorProfile == null)
            throw new IllegalArgumentException("Mentor data cannot be empty");

        return ResponseEntity.status(HttpStatus.CREATED).body(mentorService.addMentor(mentorProfile));
    }

    @PostMapping("/{mentorId}/skills/{skillId}")
    public ResponseEntity<Void> linkSkillToMentor(@PathVariable Long mentorId, @PathVariable Long skillId) {
        if (mentorId <= 0 || skillId <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        mentorService.linkExistingSkillToMentor(mentorId, skillId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/my-skills/link/{skillId}")
    public ResponseEntity<MentorSummaryDto> linkExistingSkillToCurrentMentor(@PathVariable Long skillId) {
        return ResponseEntity.ok(mentorService.linkExistingSkillToCurrentMentor(skillId));
    }

    @PostMapping("/my-skills")
    public ResponseEntity<MentorSummaryDto> addSkillToCurrentMentor(@RequestBody Skill skill) {
        if (skill == null)
            throw new IllegalArgumentException("Skill data cannot be empty");

        return ResponseEntity.status(HttpStatus.CREATED).body(mentorService.addSkillToCurrentMentor(skill));
    }

    @PostMapping("/{mentorId}/add-student")
    public ResponseEntity<Void> enrollStudent(
            @PathVariable Long mentorId,
            @RequestParam Long studentId,
            @RequestParam String skillName) {
        if (mentorId <= 0 || studentId <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        if (skillName == null || skillName.isBlank())
            throw new IllegalArgumentException("Skill name cannot be empty");

        mentorService.addLearnerToMentor(mentorId, studentId, skillName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/add-skill")
    public ResponseEntity<String> addSkillToMentor(@PathVariable Long id, @RequestParam Skill skill) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        if (skill == null)
            throw new IllegalArgumentException("Skill cannot be empty");

        mentorService.addSkillToUser(id, skill);
        return ResponseEntity.status(HttpStatus.CREATED).body("Skill " + skill.getName() + " added successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<MentorSummaryDto> updateMentor(@PathVariable Long id, @RequestBody MentorProfile mentorProfile) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        if (mentorProfile == null)
            throw new IllegalArgumentException("Update data cannot be empty");

        return ResponseEntity.ok(mentorService.updateMentor(id, mentorProfile));
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<MentorSummaryDto> updateAvailability(
            @PathVariable Long id,
            @RequestBody List<AvailabilityDto> availabilityDtos) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        if (availabilityDtos == null || availabilityDtos.isEmpty())
            throw new IllegalArgumentException("Availability list cannot be empty");

        return ResponseEntity.ok(mentorService.updateMentorAvailability(id, availabilityDtos));
    }

    @PutMapping("/{id}/capacity")
    public ResponseEntity<MentorSummaryDto> updateCapacity(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        if (body == null || !body.containsKey("maxStudents"))
            throw new IllegalArgumentException("Field maxStudents is required");

        Integer maxStudents = body.get("maxStudents");
        if (maxStudents == null || maxStudents < 0)
            throw new IllegalArgumentException("maxStudents must be a positive number");

        return ResponseEntity.ok(mentorService.updateMentorCapacity(id, maxStudents));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMentor(@PathVariable Long id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        mentorService.deleteMentor(id);
        return ResponseEntity.noContent().build();
    }
}