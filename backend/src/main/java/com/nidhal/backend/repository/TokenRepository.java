package com.nidhal.backend.repository;

import com.nidhal.backend.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * This interface represents the repository that will manage the persistence of the token entity.
 * It extends the JpaRepository interface provided by Spring Data JPA.
 */

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query(value = """
            select t from Token t inner join User u\s
            on t.user.id = u.id\s
            where u.id = :id and (t.expired = false or t.revoked = false)\s
            """)
    List<Token> findAllValidTokenByUser(Long id);


    @Query("""
            SELECT t FROM Token t
            WHERE t.token = :token
             """)
    Optional<Token> findByToken(String token);


}
