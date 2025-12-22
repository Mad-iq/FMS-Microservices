package com.gateway.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.model.User;
import com.gateway.security.JwtUtil;
import com.gateway.service.UserService;
import org.springframework.security.core.Authentication;


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
        String reqrole = req.get("role");

        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "username, email and password required"));
        }
        
        if (reqrole != null && "ADMIN".equalsIgnoreCase(reqrole)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "ADMIN role cannot be self-assigned"));
        }
        String role = "USER";

        try {
            User saved = userService.registerUser(username, email, password, role);
            return ResponseEntity.ok(
                    Map.of(
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

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(
                Map.of("token", token,"username", user.getUsername(),"email", user.getEmail(),"role", user.getRole() ));
    }
    
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> req, Authentication authentication) {

        String oldPassword = req.get("oldPassword");
        String newPassword = req.get("newPassword");

        if (oldPassword == null || newPassword == null){
            return ResponseEntity.badRequest().body(Map.of("error", "oldPassword and newPassword are required"));
        }
        String username = authentication.getName();

        try{
            userService.changePassword(username, oldPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        }catch (IllegalArgumentException ex) {
            return ResponseEntity.status(403).body(Map.of("error", ex.getMessage()));
        }
    }

}
