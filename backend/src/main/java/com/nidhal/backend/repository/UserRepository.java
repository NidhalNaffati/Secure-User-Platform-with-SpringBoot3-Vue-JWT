package com.nidhal.backend.repository;

import com.nidhal.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * This interface represents the repository that will manage the persistence of the user entity.
 * It extends the JpaRepository interface provided by Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role= 'ROLE_USER'")
    List<User> findAllUsers();

    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_USER' AND u.accountNonLocked = FALSE")
    List<User> findLockedUsers();

    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_USER' AND u.accountNonLocked = TRUE")
    List<User> findUnlockedUsers();

}