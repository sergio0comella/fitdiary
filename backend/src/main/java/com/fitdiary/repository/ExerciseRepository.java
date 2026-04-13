package com.fitdiary.repository;

import com.fitdiary.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {
    List<Exercise> findByIsCustomFalseOrderByNameAsc();

    @Query("SELECT e FROM Exercise e WHERE e.isCustom = false OR e.createdBy.id = :userId ORDER BY e.name ASC")
    List<Exercise> findAvailableForUser(@Param("userId") UUID userId);

    List<Exercise> findByNameContainingIgnoreCaseAndIsCustomFalse(String name);
}
