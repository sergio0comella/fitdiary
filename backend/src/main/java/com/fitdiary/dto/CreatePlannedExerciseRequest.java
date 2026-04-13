package com.fitdiary.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreatePlannedExerciseRequest {
    @NotNull UUID exerciseId;
    Integer exerciseOrder;
    Integer sets;
    String repsRange;
    Integer restSeconds;
    BigDecimal targetWeightKg;
    String notes;
}
