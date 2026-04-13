package com.fitdiary.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreatePlanRequest {
    @NotBlank String name;
    String split;
    Integer daysPerWeek;
    String goal;
    List<CreateWorkoutDayRequest> workoutDays;
}
