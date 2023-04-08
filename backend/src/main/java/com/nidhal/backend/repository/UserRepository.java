package com.nidhal.backend.repository;

import com.nidhal.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    // create the query to find all the users that are not enabled
    @Query("SELECT u FROM User u WHERE u.enabled = false")
    List<User> findAllByEnabledIsFalse();

    @Query("DELETE FROM User u WHERE u.id = ?1")
    @Modifying
    void deleteUserById(Long userId);
}