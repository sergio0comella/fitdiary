package com.fitdiary.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AddSetRequest {
    @NotNull UUID sessionId;
    @NotNull UUID exerciseLogId;
    Integer setNumber;
    @NotNull BigDecimal weightKg;
    @NotNull Integer reps;
    Integer rir;
    Integer durationSeconds;
    Boolean isCompleted;
    String notes;
}
