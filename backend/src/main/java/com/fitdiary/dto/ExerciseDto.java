package com.fitdiary.dto;

import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExerciseDto {
    UUID id;
    String name;
    String muscleGroup;
    String category;
    Boolean isCustom;
    String notes;
}
