package com.example.hobbiesmanagment.DTO;

import com.example.hobbiesmanagment.Entities.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {

    @Pattern(regexp = "^[a-zA-Zא-ת\\s]+$", message = "Name must contain letters only")
    private String name;

    @Email(message = "Please enter a valid email address")
    private String email;

    // Optional — only validated if provided
    @Size(min = 6, message = "Minimum 6 characters required")
    private String password;

    @Pattern(regexp = "^[0-9+\\-\\s]+$", message = "Phone number must contain digits only")
    private String phone;

    private String city;
    private String selectedRole;
    private LocalDate birthday;
    private String description;
    private Gender gender;
}
