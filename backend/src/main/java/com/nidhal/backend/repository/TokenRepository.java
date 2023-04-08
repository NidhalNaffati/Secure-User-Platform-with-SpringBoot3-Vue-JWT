package com.nidhal.backend.repository;

import com.nidhal.backend.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query(value = """
            select t from Token t inner join User u\s
            on t.user.id = u.id\s
            where u.id = :id and (t.expired = false or t.revoked = false)\s
            """)
    List<Token> findAllValidTokenByUser(Long id);



    Optional<Token> findByToken(String token);

    // create the query that deletes all the tokens of a user
    @Modifying
    @Query("DELETE FROM Token t WHERE t.user.id = ?1")
    void deleteAllByUserId(Long userId);

    @Query("SELECT t FROM Token t WHERE t.revoked = true OR t.expired = true")
    List<Token> findByRevokedTrueOrExpiredTrue();
}
