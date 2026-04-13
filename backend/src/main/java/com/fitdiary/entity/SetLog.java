package com.fitdiary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "set_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SetLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_log_id", nullable = false)
    private ExerciseLog exerciseLog;

    @Column(name = "set_number", nullable = false)
    private Integer setNumber;

    @Column(name = "weight_kg", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal weightKg = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer reps = 0;

    @Builder.Default
    private Integer rir = 2;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = true;

    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
