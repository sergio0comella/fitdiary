package com.fitdiary.dto;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExerciseLogDto {
    UUID id;
    UUID exerciseId;
    String exerciseName;
    String muscleGroup;
    Integer exerciseOrder;
    List<SetLogDto> sets;
}
