package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.CreateWaitlistEntryDto;
import com.example.hobbiesmanagment.DTO.WaitlistResponseDto;
import com.example.hobbiesmanagment.Entities.WaitlistStatus;
import com.example.hobbiesmanagment.Service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/waitlist")
public class WaitListController {

    private final WaitlistService waitlistService;

    @GetMapping
    public ResponseEntity<List<WaitlistResponseDto>> getAllEntries() {
        List<WaitlistResponseDto> list = waitlistService.getWaitlistEntry();
        if (list.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WaitlistResponseDto> getEntryById(@PathVariable Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(waitlistService.getWaitlistEntryById(id));
    }

    @PostMapping("/add")
    public ResponseEntity<WaitlistResponseDto> addToWaitlist(@RequestBody CreateWaitlistEntryDto createDto) {
        if (createDto == null)
            throw new IllegalArgumentException("Waitlist request data cannot be empty");

        WaitlistResponseDto savedDto = waitlistService.addWaitListEntry(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}/enroll")
    public ResponseEntity<Void> enrollFromWaitlist(@PathVariable Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        waitlistService.enrollFromWaitlist(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        waitlistService.deleteWaitListEntry(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<WaitlistResponseDto> updateStatus(
            @PathVariable Long id,
            @RequestParam WaitlistStatus status) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");
        if (status == null)
            throw new IllegalArgumentException("Status is invalid");

        return ResponseEntity.ok(waitlistService.updateWaitListEntry(status, id));
    }

    @GetMapping("/by-learner/{learnerId}")
    public ResponseEntity<List<WaitlistResponseDto>> getEntriesByLearner(@PathVariable Long learnerId) {
        if (learnerId == null || learnerId <= 0)
            throw new IllegalArgumentException("Learner ID must be a positive number");

        return ResponseEntity.ok(waitlistService.getWaitlistEntriesByLearnerId(learnerId));
    }

    @GetMapping("/incoming/{mentorId}")
    public ResponseEntity<List<WaitlistResponseDto>> getIncomingRequests(@PathVariable Long mentorId) {
        if (mentorId == null || mentorId <= 0)
            throw new IllegalArgumentException("Mentor ID must be a positive number");

        return ResponseEntity.ok(waitlistService.getIncomingRequestsByMentor(mentorId));
    }
}