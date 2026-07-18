package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.AvailabilityDto;
import com.example.hobbiesmanagment.Entities.Availability;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<List<AvailabilityDto>> getAllAvailability() {
        try {
            List<AvailabilityDto> list = availabilityService.getAllAvailability();
            if (list.isEmpty())
                return ResponseEntity.noContent().build();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAvailabilityById(@PathVariable Long id) {
        try {
            if (id <= 0)
                return ResponseEntity.badRequest().body("ID must be a positive number");

            return ResponseEntity.ok(availabilityService.getAvailabilityById(id));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Server error");
        }
    }

    @PostMapping
    public ResponseEntity<?> addAvailability(@RequestBody Availability availability) {
        try {
            if (availability == null)
                return ResponseEntity.badRequest().body("Availability data cannot be empty");

            availabilityService.addAvailability(availability);
            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid data: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Server error");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAvailability(@PathVariable Long id, @RequestBody Availability availability) {
        try {
            if (id <= 0)
                return ResponseEntity.badRequest().body("ID must be a positive number");

            if (availability == null)
                return ResponseEntity.badRequest().body("Update data cannot be empty");

            availabilityService.updateAvailability(id, availability);
            return ResponseEntity.ok().build();

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid data: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Server error");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAvailability(@PathVariable Long id) {
        try {
            if (id <= 0)
                return ResponseEntity.badRequest().body("ID must be a positive number");

            availabilityService.deleteAvailabilityById(id);
            return ResponseEntity.noContent().build();

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Server error");
        }
    }
}