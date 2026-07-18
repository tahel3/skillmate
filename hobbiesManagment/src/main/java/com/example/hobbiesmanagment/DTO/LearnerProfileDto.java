package com.example.hobbiesmanagment.DTO;

import com.example.hobbiesmanagment.Entities.Gender;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LearnerProfileDto {
    private String email;
    private String name;
    private String city;
    private String phone;
    private LocalDate birthday;
    private Gender gender;
    private String description;
    private int enrolledCoursesCount;
}
