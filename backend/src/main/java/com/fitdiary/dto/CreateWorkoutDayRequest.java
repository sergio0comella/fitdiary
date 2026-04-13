package com.fitdiary.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateWorkoutDayRequest {
    @NotBlank String name;
    Integer dayOrder;
    List<CreatePlannedExerciseRequest> exercises;
}
