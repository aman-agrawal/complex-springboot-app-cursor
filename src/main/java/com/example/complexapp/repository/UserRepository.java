package com.example.complexapp.repository;

import com.example.complexapp.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(User.UserRole role);
    
    List<User> findByStatus(User.UserStatus status);
    
    List<User> findByRoleAndStatus(User.UserRole role, User.UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :maxAttempts")
    List<User> findUsersWithFailedLoginAttempts(@Param("maxAttempts") int maxAttempts);
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false")
    List<User> findUnverifiedUsers();
    
    @Query("SELECT u FROM User u WHERE u.accountNonLocked = false")
    List<User> findLockedUsers();
    
    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    Page<User> findUsersByRegistrationDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") User.UserStatus status);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true")
    long countVerifiedUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :date")
    long countActiveUsers(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.createdAt >= :date")
    List<User> findRecentUsers(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL OR u.lastLogin < :date")
    List<User> findInactiveUsersSince(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts > 0")
    List<User> findUsersWithFailedLogins();
    
    @Query("SELECT u FROM User u WHERE u.lockTime IS NOT NULL AND u.lockTime < :unlockTime")
    List<User> findUsersToUnlock(@Param("unlockTime") LocalDateTime unlockTime);
}
