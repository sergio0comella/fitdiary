package com.fitdiary.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InsightDto {
    String type;
    String message;
    String exerciseName;
}
