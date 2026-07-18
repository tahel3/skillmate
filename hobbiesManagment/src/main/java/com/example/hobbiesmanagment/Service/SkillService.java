package com.example.hobbiesmanagment.Service;

import com.example.hobbiesmanagment.DTO.SkillSearchResultDto;
import com.example.hobbiesmanagment.Entities.Level;
import com.example.hobbiesmanagment.Entities.Skill;
import com.example.hobbiesmanagment.Exception.DuplicateResourceException;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Repositories.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    public Skill getSkillById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
    }

    public Skill getSkillByName(String name) {
        return skillRepository.findByName(name);
    }

    public Skill addSkill(Skill skill) {
        if (skillRepository.findByName(skill.getName()) != null) {
            throw new DuplicateResourceException("Skill with this name already exists");
        }
        skill.setId(null);
        return skillRepository.save(skill);
    }

    public void deleteSkill(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete: Skill not found");
        }
        skillRepository.deleteById(id);
    }

    public Skill editSkill(Long id, Skill updatedSkill) {
        Skill existingSkill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        existingSkill.setName(updatedSkill.getName());
        existingSkill.setDescription(updatedSkill.getDescription());
        existingSkill.setCategory(updatedSkill.getCategory());
        existingSkill.setLevel(updatedSkill.getLevel());
        existingSkill.setImage(updatedSkill.getImage());
        existingSkill.setCost(updatedSkill.getCost());
        return skillRepository.save(existingSkill);
    }

    public Page<Skill> getSkillsPage(String name, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        String nameParam = (name == null || name.isBlank()) ? null : name.trim();
        String categoryParam = (category == null || category.isBlank()) ? null : category.trim();
        return skillRepository.findSkillsByCriteria(nameParam, categoryParam, pageable);
    }

    public Page<SkillSearchResultDto> getSkillsPageAsDto(String name, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        String nameParam = (name == null || name.isBlank()) ? null : name.trim();
        String categoryParam = (category == null || category.isBlank()) ? null : category.trim();
        return skillRepository.findSkillsByCriteria(nameParam, categoryParam, pageable)
                .map(SkillSearchResultDto::fromSkill);
    }

    public List<Skill> getByCost(double cost) {
        return skillRepository.findByCost(cost);
    }

    public List<Skill> getByLevel(Level level) {
        return skillRepository.getSkillsByLevel(level);
    }

    public List<Skill> getMentorSkills(Long id) {
        return skillRepository.getMentorProfileSkillsByUserId(id);
    }
}