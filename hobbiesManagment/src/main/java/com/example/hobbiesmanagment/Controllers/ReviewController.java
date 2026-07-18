package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.ReviewRequestDTO;
import com.example.hobbiesmanagment.Service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewRequestDTO>> getAllReviews() {
        List<ReviewRequestDTO> list = reviewService.getAllReviews();
        if (list.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<ReviewRequestDTO>> getReviewsByMentor(@PathVariable Long mentorId) {
        return ResponseEntity.ok(reviewService.getReviewsByMentor(mentorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewRequestDTO> getReviewById(@PathVariable Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @PostMapping
    public ResponseEntity<ReviewRequestDTO> addReview(@RequestBody ReviewRequestDTO review) {
        if (review == null)
            throw new IllegalArgumentException("Review data cannot be empty");

        ReviewRequestDTO created = reviewService.addReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewRequestDTO> updateReview(@PathVariable Long id, @RequestBody ReviewRequestDTO review) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        if (review == null)
            throw new IllegalArgumentException("Update data cannot be empty");

        return ResponseEntity.ok(reviewService.updateReview(id, review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        reviewService.deleteReviewById(id);
        return ResponseEntity.noContent().build();
    }
}