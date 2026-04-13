package com.fitdiary.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FinishSessionRequest {
    @NotNull UUID sessionId;
    LocalDateTime finishedAt;
    String notes;
    List<LoggedExerciseRequest> exercises;
}
