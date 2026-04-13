package com.fitdiary.repository;

import com.fitdiary.entity.PlannedExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlannedExerciseRepository extends JpaRepository<PlannedExercise, UUID> {
}
