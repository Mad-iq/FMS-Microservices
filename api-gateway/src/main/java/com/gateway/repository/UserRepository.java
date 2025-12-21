package com.gateway.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gateway.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

