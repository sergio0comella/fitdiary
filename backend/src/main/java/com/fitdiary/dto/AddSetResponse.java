package com.fitdiary.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AddSetResponse {
    private UUID setId;
    private Integer setNumber;
    private BigDecimal weightKg;
    private Integer reps;
}
