package com.example.complexapp.service;

import com.example.complexapp.domain.User;
import com.example.complexapp.dto.UserRegistrationDto;
import com.example.complexapp.dto.UserUpdateDto;
import com.example.complexapp.exception.ResourceNotFoundException;
import com.example.complexapp.exception.UserAlreadyExistsException;
import com.example.complexapp.repository.UserRepository;
import com.example.complexapp.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));

        return UserPrincipal.create(user);
    }

    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Cacheable(value = "users", key = "#username")
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }

    public User createUser(UserRegistrationDto registrationDto) {
        // Check if user already exists
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + registrationDto.getUsername());
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + registrationDto.getEmail());
        }

        User user = User.builder()
                .username(registrationDto.getUsername())
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .phoneNumber(registrationDto.getPhoneNumber())
                .role(User.UserRole.USER)
                .status(User.UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        
        // Send welcome email
        emailService.sendWelcomeEmail(savedUser);
        
        log.info("Created new user: {}", savedUser.getUsername());
        return savedUser;
    }

    @CacheEvict(value = "users", key = "#id")
    public User updateUser(Long id, UserUpdateDto updateDto) {
        User user = getUserById(id);
        
        if (updateDto.getFirstName() != null) {
            user.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            user.setLastName(updateDto.getLastName());
        }
        if (updateDto.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDto.getPhoneNumber());
        }
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new UserAlreadyExistsException("Email already exists: " + updateDto.getEmail());
            }
            user.setEmail(updateDto.getEmail());
            user.setEmailVerified(false);
        }

        User updatedUser = userRepository.save(user);
        log.info("Updated user: {}", updatedUser.getUsername());
        return updatedUser;
    }

    @CacheEvict(value = "users", key = "#id")
    public User updateUserRole(Long id, User.UserRole role) {
        User user = getUserById(id);
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        log.info("Updated user role: {} -> {}", user.getUsername(), role);
        return updatedUser;
    }

    @CacheEvict(value = "users", key = "#id")
    public User updateUserStatus(Long id, User.UserStatus status) {
        User user = getUserById(id);
        user.setStatus(status);
        
        if (status == User.UserStatus.SUSPENDED) {
            user.setAccountNonLocked(false);
            user.setLockTime(LocalDateTime.now());
        } else if (status == User.UserStatus.ACTIVE) {
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            user.setFailedLoginAttempts(0);
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Updated user status: {} -> {}", user.getUsername(), status);
        return updatedUser;
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        User user = getUserById(id);
        user.setStatus(User.UserStatus.DELETED);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("Deleted user: {}", user.getUsername());
    }

    public void recordFailedLoginAttempt(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountNonLocked(false);
                user.setLockTime(LocalDateTime.now());
                user.setStatus(User.UserStatus.SUSPENDED);
            }
            
            userRepository.save(user);
            log.warn("Failed login attempt for user: {} (attempts: {})", username, user.getFailedLoginAttempts());
        });
    }

    public void recordSuccessfulLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            userRepository.save(user);
            log.info("Successful login for user: {}", username);
        });
    }

    public boolean isUserLocked(String username) {
        return userRepository.findByUsername(username)
                .map(User::isAccountLocked)
                .orElse(false);
    }

    public void verifyEmail(Long userId) {
        User user = getUserById(userId);
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getUsername());
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
        
        log.info("Password changed for user: {}", user.getUsername());
    }

    public void resetPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // Generate temporary password
            String tempPassword = generateTemporaryPassword();
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setCredentialsNonExpired(false);
            userRepository.save(user);
            
            // Send password reset email
            emailService.sendPasswordResetEmail(user, tempPassword);
            
            log.info("Password reset for user: {}", user.getUsername());
        });
    }

    private String generateTemporaryPassword() {
        return "Temp" + System.currentTimeMillis() % 10000;
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }

    public long getActiveUserCount() {
        return userRepository.countByStatus(User.UserStatus.ACTIVE);
    }
}
