package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.CreateSessionDto;
import com.example.hobbiesmanagment.DTO.SessionDto;
import com.example.hobbiesmanagment.Service.StudentSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class StudentSessionController {

    private final StudentSessionService studentSessionService;

    @GetMapping
    public ResponseEntity<List<SessionDto>> getAllSessions() {
        List<SessionDto> list = studentSessionService.getAllStudentSessions();
        return ResponseEntity.ok(list); // Returning an empty array is friendlier for React than 204
    }

    @GetMapping("/by-learner/{learnerId}")
    public ResponseEntity<List<SessionDto>> getSessionsByLearner(@PathVariable Long learnerId) {
        if (learnerId == null || learnerId <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(studentSessionService.getSessionsByLearnerId(learnerId));
    }

    @GetMapping("/by-mentor/{mentorId}")
    public ResponseEntity<List<SessionDto>> getSessionsByMentor(@PathVariable Long mentorId) {
        if (mentorId == null || mentorId <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(studentSessionService.getSessionsByMentorId(mentorId));
    }

    // (\\d+) regex ensures this route only matches numeric IDs, avoiding collisions with paths like "my"
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<SessionDto> getSessionById(@PathVariable Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(studentSessionService.getStudentSessionById(id));
    }

    @PostMapping
    public ResponseEntity<SessionDto> createSession(@RequestBody CreateSessionDto dto) {
        if (dto == null)
            throw new IllegalArgumentException("Session data cannot be empty");

        SessionDto created = studentSessionService.addStudentSession(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionDto> updateSession(@PathVariable Long id, @RequestBody CreateSessionDto dto) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");
        if (dto == null)
            throw new IllegalArgumentException("Update data cannot be empty");

        return ResponseEntity.ok(studentSessionService.updateStudentSession(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        studentSessionService.deleteStudentSession(id);
        return ResponseEntity.noContent().build();
    }
}