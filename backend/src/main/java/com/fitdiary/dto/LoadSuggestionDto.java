package com.fitdiary.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoadSuggestionDto {
    UUID exerciseId;
    String exerciseName;
    BigDecimal suggestedWeightKg;
    String reason;
    BigDecimal estimated1rm;
}
