package com.fitdiary.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VolumeDataPoint {
    String week;
    Double volumeKg;
    Integer sessions;
}
