package com.fitdiary.repository;

import com.fitdiary.entity.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, UUID> {
    List<WorkoutPlan> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<WorkoutPlan> findByUserIdAndIsActiveTrue(UUID userId);
}
