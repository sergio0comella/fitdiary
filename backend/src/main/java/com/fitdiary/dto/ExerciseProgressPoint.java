package com.fitdiary.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExerciseProgressPoint {
    LocalDateTime date;
    BigDecimal maxWeightKg;
    Integer maxReps;
    BigDecimal estimated1rm;
}
