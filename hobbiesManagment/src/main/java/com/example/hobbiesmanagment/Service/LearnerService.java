package com.example.hobbiesmanagment.Service;

import com.example.hobbiesmanagment.DTO.LearnerProfileDto;
import com.example.hobbiesmanagment.DTO.SessionDto;
import com.example.hobbiesmanagment.Entities.LearnerProfile;
import com.example.hobbiesmanagment.Entities.Skill;
import com.example.hobbiesmanagment.Entities.User;
import com.example.hobbiesmanagment.Exception.DuplicateResourceException;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Repositories.LearnerProfileRepository;
import com.example.hobbiesmanagment.Repositories.SkillRepository;
import com.example.hobbiesmanagment.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LearnerService {

    private final LearnerProfileRepository learnerProfileRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public LearnerService(LearnerProfileRepository learnerProfileRepository, UserRepository userRepository,
                          SkillRepository skillRepository, ModelMapper modelMapper) {
        this.learnerProfileRepository = learnerProfileRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.modelMapper = modelMapper;
    }

    public List<LearnerProfileDto> getAllLearners() {
        return learnerProfileRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public LearnerProfileDto getLearnerById(Long learnerId) {
        LearnerProfile learner = learnerProfileRepository.findById(learnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found with id: " + learnerId));
        return convertToDto(learner);
    }
    @Transactional
    public LearnerProfileDto addLearner(LearnerProfile learnerProfile) {
        if (learnerProfile.getUser() == null || learnerProfile.getUser().getId() == null) {
            throw new IllegalArgumentException("User ID is required to create a Learner Profile");
        }

        if (learnerProfileRepository.findByUserId(learnerProfile.getUser().getId()) != null) {
            throw new DuplicateResourceException("Learner profile already exists for this user");
        }

        LearnerProfile savedLearner = learnerProfileRepository.save(learnerProfile);
        return convertToDto(savedLearner);
    }
@Transactional
    public void deleteLearner(Long id) {
        if (!learnerProfileRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete: Learner not found");
        }
        learnerProfileRepository.deleteById(id);
    }
@Transactional
    public LearnerProfileDto updateLearner(Long id, LearnerProfile learnerProfile) {
        LearnerProfile existingLearnerProfile = learnerProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found with id: " + id));

        existingLearnerProfile.setUser(learnerProfile.getUser());
        existingLearnerProfile.setHobbies(learnerProfile.getHobbies());
        existingLearnerProfile.setSessions(learnerProfile.getSessions());
        existingLearnerProfile.setFavoriteSkills(learnerProfile.getFavoriteSkills());

        LearnerProfile updatedLearner = learnerProfileRepository.save(existingLearnerProfile);
        return convertToDto(updatedLearner);
    }
    @Transactional
    public List<Skill> getFavoriteSkillsByLearnerId(Long userId) {
        LearnerProfile learner = learnerProfileRepository.findByUserId(userId);
        if (learner == null)
            throw new ResourceNotFoundException("Learner profile not found for userId: " + userId);
        return new ArrayList<>(learner.getFavoriteSkills());
    }

    @Transactional
    public void addFavoriteSkill(Long userId, Long skillId) {
        LearnerProfile learner = learnerProfileRepository.findByUserId(userId);
        if (learner == null)
            throw new ResourceNotFoundException("Learner profile not found for userId: " + userId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
        learner.getFavoriteSkills().add(skill);
        learnerProfileRepository.save(learner);
    }

    @Transactional
    public void removeFavoriteSkill(Long userId, Long skillId) {
        LearnerProfile learner = learnerProfileRepository.findByUserId(userId);
        if (learner == null)
            throw new ResourceNotFoundException("Learner profile not found for userId: " + userId);
        learner.getFavoriteSkills().removeIf(s -> s.getId().equals(skillId));
        learnerProfileRepository.save(learner);
    }
    /**
     * פונקציית עזר פנימית המרכזת את לוגיקת המיפוי והחישובים ל-DTO
     */

    private LearnerProfileDto convertToDto(LearnerProfile learner) {
        LearnerProfileDto dto = modelMapper.map(learner, LearnerProfileDto.class);
        if (learner.getHobbies() != null) {
            dto.setEnrolledCoursesCount(learner.getHobbies().size());
        }
        return dto;
    }
}