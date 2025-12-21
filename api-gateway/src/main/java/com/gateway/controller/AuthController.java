package com.gateway.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.model.User;
import com.gateway.security.JwtUtil;
import com.gateway.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> req) {

        String username = req.get("username");
        String email = req.get("email");       
        String password = req.get("password");
        String role = req.getOrDefault("role", "USER").toUpperCase();

        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "username, email and password required"));
        }

        try {
            User saved = userService.registerUser(username, email, password, role);
            return ResponseEntity.ok(
                    Map.of(
                        "id", saved.getId(),
                        "username", saved.getUsername(),
                        "email", saved.getEmail(),
                        "role", saved.getRole()
                    )
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(403).body(Map.of("error", ex.getMessage()));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }

        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty() || !userService.verifyPassword(userOpt.get(), password)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(username, userOpt.get().getRole());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
