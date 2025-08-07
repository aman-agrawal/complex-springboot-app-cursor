package com.example.complexapp.controller;

import com.example.complexapp.dto.UserRegistrationDto;
import com.example.complexapp.dto.LoginRequestDto;
import com.example.complexapp.dto.LoginResponseDto;
import com.example.complexapp.domain.User;
import com.example.complexapp.service.UserService;
import com.example.complexapp.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            
            // Record successful login
            userService.recordSuccessfulLogin(loginRequest.getUsername());

            LoginResponseDto response = LoginResponseDto.builder()
                    .token(jwt)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpirationTime(jwt))
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Record failed login attempt
            userService.recordFailedLoginAttempt(loginRequest.getUsername());
            log.warn("Login failed for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            User user = userService.createUser(registrationDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT token")
    public ResponseEntity<LoginResponseDto> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            
            if (tokenProvider.validateToken(jwt) && !tokenProvider.isTokenExpired(jwt)) {
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                String newJwt = tokenProvider.generateToken(authentication);
                
                LoginResponseDto response = LoginResponseDto.builder()
                        .token(newJwt)
                        .tokenType("Bearer")
                        .expiresIn(tokenProvider.getExpirationTime(newJwt))
                        .build();
                
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user (client should discard token)")
    public ResponseEntity<Map<String, String>> logout() {
        SecurityContextHolder.clearContext();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset email")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        try {
            userService.resetPassword(email);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset email sent if account exists");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email address")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam Long userId) {
        try {
            userService.verifyEmail(userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email verified successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/check-username")
    @Operation(summary = "Check username availability", description = "Check if username is available")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean available = true;
        try {
            userService.getUserByUsername(username);
            available = false; // User exists
        } catch (Exception e) {
            available = true; // User doesn't exist
        }
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email")
    @Operation(summary = "Check email availability", description = "Check if email is available")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean available = !userService.getUserByEmail(email).isPresent();
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        
        return ResponseEntity.ok(response);
    }
}
