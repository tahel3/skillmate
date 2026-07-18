package com.example.hobbiesmanagment.DTO;

import com.example.hobbiesmanagment.Entities.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PaymentDto {
    private LocalDateTime paymentDate;
    private double amount;
    private PaymentStatus paymentStatus;
    private String sessionName;
}
