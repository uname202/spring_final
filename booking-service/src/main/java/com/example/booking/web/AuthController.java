package com.example.booking.web;

import com.example.booking.model.User;
import com.example.booking.repo.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@RequestBody RegisterRequest req) {
        User user = User.builder()
                .username(req.getUsername())
                .password(req.getPassword()) // NOTE: not hashed for demo
                .role("USER")
                .build();
        userRepository.save(user);
        // return a dummy token placeholder
        return new TokenResponse("demo-token-for-" + user.getUsername());
    }

    @PostMapping("/auth")
    public TokenResponse auth(@RequestBody AuthRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .filter(u -> u.getPassword().equals(req.getPassword()))
                .orElseThrow(() -> new RuntimeException("Bad credentials"));
        return new TokenResponse("demo-token-for-" + user.getUsername());
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
    }

    @Data
    public static class AuthRequest {
        private String username;
        private String password;
    }

    public record TokenResponse(String token) {}
}
