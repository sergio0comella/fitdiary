package com.fitdiary.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoggedExerciseRequest {
    @NotNull UUID exerciseId;
    String exerciseName;
    String muscleGroup;
    Integer exerciseOrder;
    List<LoggedSetRequest> sets;
}
