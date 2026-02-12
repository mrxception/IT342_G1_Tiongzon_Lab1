package com.citu.ura.Controller;

import com.citu.ura.Entity.User;
import com.citu.ura.Repository.UserRepository;
import com.citu.ura.Utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtService;

    public UserController (UserRepository userRepository){ this.userRepository = userRepository; }

    @GetMapping
    public ResponseEntity<?> getAllUser(@RequestHeader("Authorization") String authorizationHeader){
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("message", "Missing or invalid Authorization header"));
        }

        String token = authorizationHeader.substring(7);
        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("message", "Invalid token"));
        }

        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId, @RequestHeader("Authorization") String authorizationHeader){
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("message", "Missing or invalid Authorization header"));
        }

        String token = authorizationHeader.substring(7);
        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("message", "Invalid token"));
        }

        return userRepository.findByUserId(userId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("message", "Missing or invalid Authorization header"));
        }

        String token = authorizationHeader.substring(7);
        String username;

        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("message", "Invalid token"));
        }

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body(Map.of("message", "User not found"));
        }
        return ResponseEntity.ok(user);
    }
}
