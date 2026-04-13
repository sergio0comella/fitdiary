package com.fitdiary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkoutPlanDto {
    UUID id;
    String name;
    String split;
    Integer daysPerWeek;
    String goal;
    Boolean isActive;
    List<WorkoutDayDto> workoutDays;
    LocalDateTime createdAt;
}
