package com.example.hobbiesmanagment.Repositories;

import com.example.hobbiesmanagment.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
   User findByCredentialName(String name);
   Optional<User> findByEmail(String email);
   Boolean existsByEmail(String email);
}

