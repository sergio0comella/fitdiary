package com.fitdiary.repository;

import com.fitdiary.entity.RefreshToken;
import com.fitdiary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    @Transactional
    void deleteByUser(User user);
    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime now);
}
