package com.fitdiary.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PersonalRecordDto {
    UUID exerciseId;
    String exerciseName;
    String muscleGroup;
    BigDecimal weightKg;
    Integer reps;
    BigDecimal estimated1rm;
    LocalDateTime achievedAt;
}
