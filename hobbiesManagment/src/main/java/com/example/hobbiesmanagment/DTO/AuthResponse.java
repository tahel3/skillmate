package com.example.hobbiesmanagment.DTO;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AuthResponse {
    private String token;
    private String role;
    private String email;
    private boolean hasProfile;
    private Long userId;
}