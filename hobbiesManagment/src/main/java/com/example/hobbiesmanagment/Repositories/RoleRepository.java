package com.example.hobbiesmanagment.Repositories;

import com.example.hobbiesmanagment.Entities.Role;
import com.example.hobbiesmanagment.Entities.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    public Optional<Role> findByName(RoleName name);
}
