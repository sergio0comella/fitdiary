package com.fitdiary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "planned_exercises")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlannedExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_day_id", nullable = false)
    private WorkoutDay workoutDay;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "exercise_order", nullable = false)
    private Integer exerciseOrder;

    @Builder.Default
    private Integer sets = 3;

    @Column(name = "reps_range")
    @Builder.Default
    private String repsRange = "8-12";

    @Column(name = "rest_seconds")
    @Builder.Default
    private Integer restSeconds = 90;

    @Column(name = "target_weight_kg", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal targetWeightKg = BigDecimal.ZERO;

    private String notes;
}
