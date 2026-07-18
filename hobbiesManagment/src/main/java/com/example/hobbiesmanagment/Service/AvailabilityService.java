package com.example.hobbiesmanagment.Service;
import com.example.hobbiesmanagment.DTO.AvailabilityDto;
import com.example.hobbiesmanagment.Entities.Availability;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Repositories.AvailabilityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final ModelMapper modelMapper;

    // Get all availability slots
    public List<AvailabilityDto> getAllAvailability() {
        return availabilityRepository.findAll()
                .stream()
                .map(availability -> modelMapper.map(availability, AvailabilityDto.class))
                .collect(Collectors.toList());
    }

    // Find by ID
    public AvailabilityDto getAvailabilityById(Long id) {
        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));
        return modelMapper.map(availability, AvailabilityDto.class);
    }

    // Add
    @Transactional
    public AvailabilityDto addAvailability(Availability availability) {
        Availability saved = availabilityRepository.save(availability);
        return modelMapper.map(saved, AvailabilityDto.class);
    }

    // Update
    @Transactional
    public AvailabilityDto updateAvailability(Long id, Availability updatedData) {
        Availability existingAvailability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot update, availability not found with id: " + id));

        existingAvailability.setDayOfWeek(updatedData.getDayOfWeek());
        existingAvailability.setStartTime(updatedData.getStartTime());
        existingAvailability.setEndTime(updatedData.getEndTime());

        Availability saved = availabilityRepository.save(existingAvailability);
        return modelMapper.map(saved, AvailabilityDto.class);
    }

    // Delete
    @Transactional
    public void deleteAvailabilityById(Long id) {
        if (!availabilityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete, availability not found with id: " + id);
        }
        availabilityRepository.deleteById(id);
    }
}