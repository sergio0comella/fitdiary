package com.fitdiary.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
    @NotBlank @Email public String email;
    @NotBlank @Size(min = 6) public String password;
    @NotBlank public String name;
    public Integer age;
    public BigDecimal weightKg;
    public BigDecimal heightCm;
    public String goal;
    public String level;
}
