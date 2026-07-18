package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.Entities.Role;
import com.example.hobbiesmanagment.Service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> list = roleService.getAllRoles();
        if (list.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @PostMapping
    public ResponseEntity<Role> addRole(@RequestBody Role role) {
        if (role == null)
            throw new IllegalArgumentException("Role data cannot be empty");

        Role created = roleService.addRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}