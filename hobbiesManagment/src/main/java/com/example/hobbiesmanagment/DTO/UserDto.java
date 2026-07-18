package com.example.hobbiesmanagment.DTO;

import com.example.hobbiesmanagment.Entities.Gender;
import com.example.hobbiesmanagment.Entities.User;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long idNumber;
    private String name;
    private String email;
    private String city;
    private String phone;
    private String selectedRole;
    private LocalDate birthday;
    private String description;
    private Gender gender;

    public static UserDto fromEntity(User user) {
        String roleName;
        if (user.getRole().isEmpty()) {
            roleName = "LEARNER";
        } else {
            boolean isMentor = user.getRole().stream()
                    .anyMatch(r -> r.getName().name().equals("MENTOR"));
            boolean isLearner = user.getRole().stream()
                    .anyMatch(r -> r.getName().name().equals("LEARNER"));

            if (isMentor && isLearner) {
                roleName = "MENTOR_AND_LEARNER";
            } else if (isMentor) {
                roleName = "MENTOR";
            } else {
                roleName = "LEARNER";
            }
        }
        return UserDto.builder()
                .idNumber(user.getId())
                .name(user.getCredential().getName())
                .email(user.getEmail())
                .city(user.getCity())
                .phone(user.getPhone())
                .selectedRole(roleName)
                .birthday(user.getBirthday())
                .description(user.getDescription())
                .gender(user.getGender())
                .build();
    }
}