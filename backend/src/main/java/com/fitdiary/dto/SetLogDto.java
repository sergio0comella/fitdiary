package com.fitdiary.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SetLogDto {
    UUID id;
    Integer setNumber;
    BigDecimal weightKg;
    Integer reps;
    Integer rir;
    Integer durationSeconds;
    Boolean isCompleted;
    String notes;
}
