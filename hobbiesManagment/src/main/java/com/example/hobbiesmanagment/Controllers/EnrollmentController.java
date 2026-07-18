package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.CreateEnrollmentDto;
import com.example.hobbiesmanagment.DTO.EnrollmentDto;
import com.example.hobbiesmanagment.Service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enrollment")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<List<EnrollmentDto>> getAllEnrollments() {
        List<EnrollmentDto> list = enrollmentService.getAllEnrollments();
        if (list.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDto> getEnrollmentById(@PathVariable Long id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(enrollmentService.getEnrollmentById(id));
    }

    @PostMapping
    public ResponseEntity<EnrollmentDto> addEnrollment(@RequestBody CreateEnrollmentDto enrollment) {
        if (enrollment == null)
            throw new IllegalArgumentException("Enrollment data cannot be empty");

        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.addEnrollment(enrollment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentDto> updateEnrollment(@PathVariable Long id, @RequestBody EnrollmentDto enrollment) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        if (enrollment == null)
            throw new IllegalArgumentException("Update data cannot be empty");

        return ResponseEntity.ok(enrollmentService.updateEnrollment(id, enrollment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollmentById(@PathVariable Long id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}