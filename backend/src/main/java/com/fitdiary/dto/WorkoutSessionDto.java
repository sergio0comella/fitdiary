package com.fitdiary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkoutSessionDto {
    UUID id;
    UUID planId;
    String planName;
    String dayName;
    LocalDateTime startedAt;
    LocalDateTime finishedAt;
    Integer durationSeconds;
    BigDecimal totalVolumeKg;
    String notes;
    List<ExerciseLogDto> exerciseLogs;
}
