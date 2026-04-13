package com.fitdiary.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdatePlannedExerciseRequest {
    Integer sets;
    String repsRange;
    Integer restSeconds;
    BigDecimal targetWeightKg;
    String notes;
}
