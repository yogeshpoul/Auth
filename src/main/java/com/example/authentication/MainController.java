package com.example.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api")
public class MainController {

    @Autowired
    private UserRepository userRepository;

    // 1. Sign up route
    @PostMapping(path = "/signup")
    public @ResponseBody User signup(@RequestBody User user) {
        userRepository.save(user);
        return user;
    }

    // 2. Sign in route
    @PostMapping(path = "/signin")
    public ResponseEntity<?> signin(@RequestBody User user) {
        // Check if the user exists
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser == null || !existingUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        // Generate JWT token
        String token = JwtUtil.generateToken(existingUser.getEmail());

        // Return the token
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    // 3. Hello route (requires valid JWT)
    @GetMapping(path = "/hello")
    public ResponseEntity<?> hello(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // Extract token and validate
        String token = authHeader.replace("Bearer ", "");
        String email = JwtUtil.validateAndGetEmail(token);

        if (email == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // Return restricted response
        return ResponseEntity.ok("Hello, " + email + "!");
    }
}
