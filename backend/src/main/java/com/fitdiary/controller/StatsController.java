package com.fitdiary.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fitdiary.dto.ApiResponse;
import com.fitdiary.dto.ExerciseProgressPoint;
import com.fitdiary.dto.InsightDto;
import com.fitdiary.dto.LoadSuggestionDto;
import com.fitdiary.dto.PersonalRecordDto;
import com.fitdiary.dto.StatsDto;
import com.fitdiary.dto.VolumeDataPoint;
import com.fitdiary.service.StatsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Stats", description = "Statistiche e progressi")
class StatsController {
    private final StatsService statsService;

    @GetMapping
    @Operation(summary = "Statistiche generali utente")
    public ResponseEntity<ApiResponse<StatsDto>> getStats(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok(statsService.getStats(UUID.fromString(ud.getUsername()))));
    }

    @GetMapping("/volume")
    @Operation(summary = "Volume settimanale")
    public ResponseEntity<ApiResponse<List<VolumeDataPoint>>> getVolume(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "12") int weeks) {
        return ResponseEntity.ok(ApiResponse.ok(
                statsService.getVolumeByWeek(UUID.fromString(ud.getUsername()), weeks)));
    }

    @GetMapping("/progress/exercise/{exerciseId}")
    @Operation(summary = "Progressione per esercizio")
    public ResponseEntity<ApiResponse<List<ExerciseProgressPoint>>> getExerciseProgress(
            @AuthenticationPrincipal UserDetails ud, @PathVariable UUID exerciseId) {
        return ResponseEntity.ok(ApiResponse.ok(
                statsService.getExerciseProgress(UUID.fromString(ud.getUsername()), exerciseId)));
    }

    @GetMapping("/prs")
    @Operation(summary = "Personal records")
    public ResponseEntity<ApiResponse<List<PersonalRecordDto>>> getPRs(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok(statsService.getPersonalRecords(UUID.fromString(ud.getUsername()))));
    }

    @GetMapping("/insights")
    @Operation(summary = "Insight e suggerimenti automatici")
    public ResponseEntity<ApiResponse<List<InsightDto>>> getInsights(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok(statsService.getInsights(UUID.fromString(ud.getUsername()))));
    }

    @GetMapping("/suggest/{exerciseId}")
    @Operation(summary = "Suggerimento carico per un esercizio")
    public ResponseEntity<ApiResponse<LoadSuggestionDto>> getSuggestedLoad(
            @AuthenticationPrincipal UserDetails ud, @PathVariable UUID exerciseId) {
        return ResponseEntity.ok(ApiResponse.ok(
                statsService.getSuggestedLoad(UUID.fromString(ud.getUsername()), exerciseId)));
    }
}