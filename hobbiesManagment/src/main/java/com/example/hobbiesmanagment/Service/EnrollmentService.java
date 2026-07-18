package com.example.hobbiesmanagment.Service;

import com.example.hobbiesmanagment.DTO.CreateEnrollmentDto;
import com.example.hobbiesmanagment.DTO.EnrollmentDto;
import com.example.hobbiesmanagment.Entities.Enrollment;
import com.example.hobbiesmanagment.Entities.LearnerProfile;
import com.example.hobbiesmanagment.Entities.MentorProfile;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Repositories.EnrollmentRepository;
import com.example.hobbiesmanagment.Repositories.LearnerProfileRepository;
import com.example.hobbiesmanagment.Repositories.MentorProfileRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final ModelMapper modelMapper;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             MentorProfileRepository mentorProfileRepository,
                             LearnerProfileRepository learnerProfileRepository,
                             ModelMapper modelMapper) {
        this.enrollmentRepository = enrollmentRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.learnerProfileRepository = learnerProfileRepository;
        this.modelMapper = modelMapper;
    }

    public List<EnrollmentDto> getAllEnrollments() {
        return enrollmentRepository.findAll()
                .stream()
                .map(enrollment -> modelMapper.map(enrollment, EnrollmentDto.class))
                .collect(Collectors.toList());
    }

    public EnrollmentDto getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id));
        return modelMapper.map(enrollment, EnrollmentDto.class);
    }

    public EnrollmentDto addEnrollment(CreateEnrollmentDto createDto) {
        MentorProfile mentor = mentorProfileRepository.findById(createDto.getMentorId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + createDto.getMentorId()));

        LearnerProfile learner = learnerProfileRepository.findByUserId(createDto.getLearnerId());
        if (learner == null) {
            throw new ResourceNotFoundException("Learner profile not found for user id: " + createDto.getLearnerId());
        }

        Enrollment enrollment = Enrollment.builder()
                .mentor(mentor)
                .learner(learner)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        return modelMapper.map(saved, EnrollmentDto.class);
    }

    public EnrollmentDto updateEnrollment(Long id, EnrollmentDto updateDto) {
        Enrollment existingEnrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot update, enrollment not found"));
        existingEnrollment.setActive(updateDto.isActive());
        Enrollment updatedEnrollment = enrollmentRepository.save(existingEnrollment);
        return modelMapper.map(updatedEnrollment, EnrollmentDto.class);
    }

    public void deleteEnrollment(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete, enrollment not found");
        }
        enrollmentRepository.deleteById(id);
    }
}