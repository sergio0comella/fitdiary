package com.fitdiary.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fitdiary.dto.ApiResponse;
import com.fitdiary.dto.CreatePlanRequest;
import com.fitdiary.dto.CreatePlannedExerciseRequest;
import com.fitdiary.dto.UpdatePlannedExerciseRequest;
import com.fitdiary.dto.WorkoutPlanDto;
import com.fitdiary.service.WorkoutService;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Plans", description = "Gestione schede di allenamento")
class WorkoutPlanController {
    private final WorkoutService workoutService;

    @GetMapping
    @Operation(summary = "Ottieni tutte le schede dell'utente")
    public ResponseEntity<ApiResponse<List<WorkoutPlanDto>>> getPlans(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok(workoutService.getPlans(uuid(ud))));
    }

    @PostMapping
    @Operation(summary = "Crea una nuova scheda")
    public ResponseEntity<ApiResponse<WorkoutPlanDto>> createPlan(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody CreatePlanRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(workoutService.createPlan(uuid(ud), req)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ottieni scheda per ID")
    public ResponseEntity<ApiResponse<WorkoutPlanDto>> getPlan(
            @AuthenticationPrincipal UserDetails ud, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(workoutService.getPlan(uuid(ud), id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una scheda")
    public ResponseEntity<ApiResponse<Void>> deletePlan(
            @AuthenticationPrincipal UserDetails ud, @PathVariable UUID id) {
        workoutService.deletePlan(uuid(ud), id);
        return ResponseEntity.ok(ApiResponse.ok("Scheda eliminata", null));
    }

    @PostMapping("/{planId}/days/{dayId}/exercises")
    @Operation(summary = "Aggiungi esercizio a un giorno della scheda")
    public ResponseEntity<ApiResponse<WorkoutPlanDto>> addExercise(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @Valid @RequestBody CreatePlannedExerciseRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(workoutService.addExerciseToDay(uuid(ud), planId, dayId, req)));
    }

    @PutMapping("/{planId}/days/{dayId}/exercises/{exerciseId}")
    @Operation(summary = "Modifica un esercizio nella scheda")
    public ResponseEntity<ApiResponse<WorkoutPlanDto>> updateExercise(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @PathVariable UUID exerciseId,
            @RequestBody UpdatePlannedExerciseRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(workoutService.updatePlannedExercise(uuid(ud), planId, exerciseId, req)));
    }

    @DeleteMapping("/{planId}/days/{dayId}/exercises/{exerciseId}")
    @Operation(summary = "Rimuovi un esercizio dalla scheda")
    public ResponseEntity<ApiResponse<WorkoutPlanDto>> deleteExercise(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @PathVariable UUID exerciseId) {
        return ResponseEntity.ok(ApiResponse.ok(workoutService.deletePlannedExercise(uuid(ud), planId, exerciseId)));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Imposta scheda come attiva")
    public ResponseEntity<ApiResponse<WorkoutPlanDto>> activatePlan(
            @AuthenticationPrincipal UserDetails ud, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(workoutService.setActivePlan(uuid(ud), id)));
    }

    private UUID uuid(UserDetails ud) { return UUID.fromString(ud.getUsername()); }
}
