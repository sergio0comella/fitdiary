package com.fitdiary.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fitdiary.dto.ApiResponse;
import com.fitdiary.dto.CreateExerciseRequest;
import com.fitdiary.dto.ExerciseDto;
import com.fitdiary.service.WorkoutService;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/exercises")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Exercises", description = "Libreria esercizi")
class ExerciseController {
    private final WorkoutService workoutService;

    @GetMapping
    @Operation(summary = "Ottieni esercizi disponibili")
    public ResponseEntity<ApiResponse<List<ExerciseDto>>> getExercises(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(
                workoutService.getExercises(UUID.fromString(ud.getUsername()), search)));
    }

    @PostMapping
    @Operation(summary = "Crea esercizio custom")
    public ResponseEntity<ApiResponse<ExerciseDto>> createExercise(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody CreateExerciseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                workoutService.createCustomExercise(UUID.fromString(ud.getUsername()), req)));
    }
}

