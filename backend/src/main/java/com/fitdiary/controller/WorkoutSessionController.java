package com.fitdiary.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fitdiary.dto.AddSetRequest;
import com.fitdiary.dto.AddSetResponse;
import com.fitdiary.dto.ApiResponse;
import com.fitdiary.dto.FinishSessionRequest;
import com.fitdiary.dto.StartSessionRequest;
import com.fitdiary.dto.WorkoutSessionDto;
import com.fitdiary.service.WorkoutService;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Sessions", description = "Logging sessioni di allenamento")
class WorkoutSessionController {
    private final WorkoutService workoutService;

    @PostMapping("/start")
    @Operation(summary = "Avvia una nuova sessione")
    public ResponseEntity<ApiResponse<WorkoutSessionDto>> startSession(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody StartSessionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(workoutService.startSession(UUID.fromString(ud.getUsername()), req)));
    }

    @PostMapping("/finish")
    @Operation(summary = "Termina e salva una sessione")
    public ResponseEntity<ApiResponse<WorkoutSessionDto>> finishSession(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody FinishSessionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(workoutService.finishSession(UUID.fromString(ud.getUsername()), req)));
    }

    @PostMapping("/sets")
    @Operation(summary = "Aggiunge una serie durante la sessione")
    public ResponseEntity<ApiResponse<AddSetResponse>> addSet(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody AddSetRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(workoutService.addSet(UUID.fromString(ud.getUsername()), req)));
    }

    @GetMapping
    @Operation(summary = "Lista sessioni recenti")
    public ResponseEntity<ApiResponse<List<WorkoutSessionDto>>> getSessions(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                workoutService.getSessions(UUID.fromString(ud.getUsername()), limit)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio sessione")
    public ResponseEntity<ApiResponse<WorkoutSessionDto>> getSession(
            @AuthenticationPrincipal UserDetails ud, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                workoutService.getSession(UUID.fromString(ud.getUsername()), id)));
    }
}
