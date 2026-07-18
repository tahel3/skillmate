package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.UserDto;
import com.example.hobbiesmanagment.DTO.UserRegistrationDto;
import com.example.hobbiesmanagment.DTO.UserUpdateDto;
import com.example.hobbiesmanagment.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> list = userService.getAllUsers();
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email cannot be empty");
        }
        UserDto userDto = userService.getUserByEmail(email);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID must be a positive number");
        }
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto updatedUser) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID must be a positive number");
        }
        return ResponseEntity.ok(userService.UpdateUser(id, updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID must be a positive number");
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}