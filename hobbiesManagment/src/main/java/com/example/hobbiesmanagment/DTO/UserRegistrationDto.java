package com.example.hobbiesmanagment.DTO;
import jakarta.validation.constraints.Pattern;
import com.example.hobbiesmanagment.Entities.Gender;
import com.example.hobbiesmanagment.Entities.Role;
import com.example.hobbiesmanagment.Entities.RoleName;
import com.example.hobbiesmanagment.Entities.User;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


import java.time.LocalDate;
import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserRegistrationDto {
    @NotBlank(message = "Name is a required field")
    @Pattern(regexp = "^[a-zA-Zא-ת\\s]+$", message = "Name must contain letters only")
    private String name;

    @NotBlank(message = "ID Number is a required field")
    @Pattern(regexp = "^\\d{9}$", message = "ID Number must be exactly 9 digits")
    private String idNumber;

    @NotBlank(message = "Email is a required field")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password is a required field")
    @Size(min = 6, message = "Minimum 6 characters required")
    private String password;

    @NotBlank(message = "Phone number is a required field")
    @Pattern(regexp = "^[0-9+\\-\\s]+$", message = "Phone number must contain digits only")
    private String phone;

    @NotBlank(message = "City is a required field")
    private String city;
    private String selectedRole;
    private LocalDate birthday;
    private String description;
    private Gender gender;


    public static UserRegistrationDto fromUser(User user, RoleName roleName) {
        return UserRegistrationDto.builder().name(user.getCredential().getName())
                .idNumber(user.getId().toString())
                .email(user.getEmail())
                .city(user.getCity())
                .phone(user.getPhone())
                .selectedRole(roleName.name()).birthday(user.getBirthday())
                .description(user.getDescription()).gender(user.getGender())
                .build();
    }
}