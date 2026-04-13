package com.fitdiary.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StartSessionRequest {
    UUID planId;
    UUID workoutDayId;
    String dayName;
    LocalDateTime startedAt;
}
