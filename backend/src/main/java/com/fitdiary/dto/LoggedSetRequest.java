package com.fitdiary.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoggedSetRequest {
    Integer setNumber;
    @NotNull BigDecimal weightKg;
    @NotNull Integer reps;
    Integer rir;
    Integer durationSeconds;
    Boolean isCompleted;
    String notes;
}
