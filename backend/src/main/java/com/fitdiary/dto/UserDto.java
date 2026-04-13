package com.fitdiary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitdiary.entity.User;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    UUID id;
    String email;
    String name;
    Integer age;
    BigDecimal weightKg;
    BigDecimal heightCm;
    String goal;
    String level;
    LocalDateTime createdAt;

    public static UserDto from(User u) {
        return UserDto.builder()
                .id(u.getId()).email(u.getEmail()).name(u.getName())
                .age(u.getAge()).weightKg(u.getWeightKg()).heightCm(u.getHeightCm())
                .goal(u.getGoal()).level(u.getLevel() != null ? u.getLevel().name() : null)
                .createdAt(u.getCreatedAt()).build();
    }
}
