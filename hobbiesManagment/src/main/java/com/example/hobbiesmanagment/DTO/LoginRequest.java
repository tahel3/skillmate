package com.example.hobbiesmanagment.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LoginRequest {
    private String password;
    private String email;
}
