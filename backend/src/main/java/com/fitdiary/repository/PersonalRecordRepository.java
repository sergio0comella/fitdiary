package com.fitdiary.repository;

import com.fitdiary.entity.PersonalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalRecordRepository extends JpaRepository<PersonalRecord, UUID> {
    List<PersonalRecord> findByUserIdOrderByWeightKgDesc(UUID userId);
    Optional<PersonalRecord> findByUserIdAndExerciseId(UUID userId, UUID exerciseId);
}
