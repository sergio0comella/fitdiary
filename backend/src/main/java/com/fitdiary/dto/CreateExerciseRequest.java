package com.fitdiary.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateExerciseRequest {
    @NotBlank String name;
    String muscleGroup;
    String category;
    String notes;
}
