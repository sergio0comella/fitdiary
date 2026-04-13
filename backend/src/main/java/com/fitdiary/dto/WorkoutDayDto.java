package com.fitdiary.dto;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkoutDayDto {
    UUID id;
    String name;
    Integer dayOrder;
    List<PlannedExerciseDto> exercises;
}
