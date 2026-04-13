package com.fitdiary.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PlannedExerciseDto {
    UUID id;
    UUID exerciseId;
    String exerciseName;
    String muscleGroup;
    Integer exerciseOrder;
    Integer sets;
    String repsRange;
    Integer restSeconds;
    BigDecimal targetWeightKg;
    String notes;        // note specifiche del piano
    String exerciseNotes; // note tecniche dalla libreria esercizi
}
