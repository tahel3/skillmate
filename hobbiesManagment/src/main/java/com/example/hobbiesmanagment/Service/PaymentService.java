package com.example.hobbiesmanagment.Service;

import com.example.hobbiesmanagment.DTO.PaymentDto;
import com.example.hobbiesmanagment.Entities.Payment;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(payment -> modelMapper.map(payment, PaymentDto.class))
                .collect(Collectors.toList());
    }

    public PaymentDto getPaymentById(long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return modelMapper.map(payment, PaymentDto.class);
    }

    @Transactional
    public PaymentDto addPayment(PaymentDto paymentDto) {
        Payment payment = modelMapper.map(paymentDto, Payment.class);
        Payment savedPayment = paymentRepository.save(payment);
        return modelMapper.map(savedPayment, PaymentDto.class);
    }

    @Transactional
    public PaymentDto updatePayment(long id, PaymentDto paymentDto) {
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        existingPayment.setAmount(paymentDto.getAmount());
        existingPayment.setStatus(paymentDto.getPaymentStatus());
        existingPayment.setPaymentDate(paymentDto.getPaymentDate());

        Payment updatedPayment = paymentRepository.save(existingPayment);
        return modelMapper.map(updatedPayment, PaymentDto.class);
    }

    @Transactional
    public void deletePayment(long id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete, payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }
}