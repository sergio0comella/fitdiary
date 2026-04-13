package com.fitdiary.repository;

import com.fitdiary.entity.ExerciseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, UUID> {
    @Query("SELECT el FROM ExerciseLog el WHERE el.session.user.id = :userId AND el.exercise.id = :exerciseId ORDER BY el.session.startedAt DESC")
    List<ExerciseLog> findByUserIdAndExerciseId(@Param("userId") UUID userId, @Param("exerciseId") UUID exerciseId);
}
