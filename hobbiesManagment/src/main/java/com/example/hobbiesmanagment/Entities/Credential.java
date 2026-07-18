package com.example.hobbiesmanagment.Entities;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
@Embeddable
public class Credential {
    private String name;
    private String password;
}
