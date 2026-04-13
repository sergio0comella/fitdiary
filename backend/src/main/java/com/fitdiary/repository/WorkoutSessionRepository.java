package com.fitdiary.repository;

import com.fitdiary.entity.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {
    List<WorkoutSession> findByUserIdOrderByStartedAtDesc(UUID userId);

    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.user.id = :userId AND ws.startedAt >= :from ORDER BY ws.startedAt DESC")
    List<WorkoutSession> findByUserIdSince(@Param("userId") UUID userId, @Param("from") LocalDateTime from);

    @Query("SELECT COUNT(ws) FROM WorkoutSession ws WHERE ws.user.id = :userId AND ws.startedAt >= :from")
    Long countByUserIdSince(@Param("userId") UUID userId, @Param("from") LocalDateTime from);

    @Query("SELECT COALESCE(SUM(ws.totalVolumeKg), 0) FROM WorkoutSession ws WHERE ws.user.id = :userId AND ws.startedAt >= :from")
    Double totalVolumeByUserIdSince(@Param("userId") UUID userId, @Param("from") LocalDateTime from);
}
