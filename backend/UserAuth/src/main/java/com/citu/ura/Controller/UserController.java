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

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> getAllUser(){
        return userRepository.findAll();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user){
        var userFound = userRepository.findByUsername(user.getUsername());
        if (userFound.isPresent()){
            if (passwordEncoder.matches(user.getPassword(), userFound.get().getPassword())){
                String token = jwtService.generateToken(userFound.get().getUsername());
                return ResponseEntity.ok(Map.of("token", token));
            }
            else{
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid credentials!"));
            }
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Username not found!"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user){
        if (userRepository.existsByUsername(user.getUsername())){
            return ResponseEntity.badRequest().body(Map.of("message", "Username already exists!"));
        }

        if (userRepository.existsByEmail(user.getEmail())){
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists!"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Registration success!"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId){
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
