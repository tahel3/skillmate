package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.PaymentDto;
import com.example.hobbiesmanagment.Service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentDto>> getAllPayments() {
        List<PaymentDto> list = paymentService.getAllPayments();
        if (list.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable long id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @PostMapping
    public ResponseEntity<PaymentDto> addPayment(@RequestBody PaymentDto payment) {
        if (payment == null)
            throw new IllegalArgumentException("Payment data cannot be empty");

        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.addPayment(payment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentDto> updatePayment(@PathVariable long id, @RequestBody PaymentDto payment) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        if (payment == null)
            throw new IllegalArgumentException("Update data cannot be empty");

        return ResponseEntity.ok(paymentService.updatePayment(id, payment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable long id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}