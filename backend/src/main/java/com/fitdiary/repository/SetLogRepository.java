package com.fitdiary.repository;

import com.fitdiary.entity.SetLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SetLogRepository extends JpaRepository<SetLog, UUID> {
    List<SetLog> findByExerciseLogId(UUID exerciseLogId);
}
