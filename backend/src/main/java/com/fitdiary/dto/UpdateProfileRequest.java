package com.fitdiary.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateProfileRequest {
    String name;
    Integer age;
    BigDecimal weightKg;
    BigDecimal heightCm;
    String goal;
    String level;
}
