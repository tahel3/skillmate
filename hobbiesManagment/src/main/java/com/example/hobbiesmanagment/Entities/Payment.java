package com.example.hobbiesmanagment.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id")
    private StudentSession session; // התשלום משויך לשיעור ספציפי

    private double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, PAID, REFUNDED

    private LocalDateTime paymentDate;
}