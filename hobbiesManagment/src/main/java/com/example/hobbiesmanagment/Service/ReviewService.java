package com.example.hobbiesmanagment.Service;

import com.example.hobbiesmanagment.DTO.ReviewRequestDTO;
import com.example.hobbiesmanagment.Entities.LearnerProfile;
import com.example.hobbiesmanagment.Entities.MentorProfile;
import com.example.hobbiesmanagment.Entities.Review;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Repositories.LearnerProfileRepository;
import com.example.hobbiesmanagment.Repositories.MentorProfileRepository;
import com.example.hobbiesmanagment.Repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Generates a clean constructor for all final fields, removing @Autowired clutter
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final ModelMapper modelMapper;

    public List<ReviewRequestDTO> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(entry -> modelMapper.map(entry, ReviewRequestDTO.class))
                .collect(Collectors.toList());
    }

    public ReviewRequestDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return modelMapper.map(review, ReviewRequestDTO.class);
    }

    @Transactional
    public ReviewRequestDTO addReview(ReviewRequestDTO reviewDto) {
        MentorProfile mentor = mentorProfileRepository.findById(reviewDto.getMentorId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + reviewDto.getMentorId()));

        LearnerProfile reviewer = learnerProfileRepository.findByUserId(reviewDto.getLearnerId());
        if (reviewer == null) {
            throw new ResourceNotFoundException("Learner profile not found for user id: " + reviewDto.getLearnerId());
        }

        Review review = Review.builder()
                .mentor(mentor)
                .reviewer(reviewer)
                .rating(reviewDto.getRating())
                .comment(reviewDto.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        return modelMapper.map(savedReview, ReviewRequestDTO.class);
    }

    public List<ReviewRequestDTO> getReviewsByMentor(Long mentorId) {
        return reviewRepository.findByMentorId(mentorId)
                .stream()
                .map(entry -> modelMapper.map(entry, ReviewRequestDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewRequestDTO updateReview(Long id, ReviewRequestDTO reviewDto) {
        // Fetch the existing entity to avoid overwriting missing fields
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        existingReview.setComment(reviewDto.getComment());
        existingReview.setRating(reviewDto.getRating());

        Review updatedReview = reviewRepository.save(existingReview);
        return modelMapper.map(updatedReview, ReviewRequestDTO.class);
    }

    @Transactional
    public void deleteReviewById(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete, review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }
}