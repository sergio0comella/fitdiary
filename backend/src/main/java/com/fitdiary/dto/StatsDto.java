package com.fitdiary.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StatsDto {
    Long totalSessions;
    Double totalVolumeKg;
    Long sessionsThisWeek;
    Double volumeThisWeek;
    Integer currentStreak;
}
