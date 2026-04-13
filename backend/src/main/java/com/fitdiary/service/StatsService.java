package com.fitdiary.service;

import com.fitdiary.dto.*;
import com.fitdiary.entity.*;
import com.fitdiary.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final WorkoutSessionRepository sessionRepository;
    private final ExerciseLogRepository exerciseLogRepository;
    private final PersonalRecordRepository prRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    public StatsDto getStats(UUID userId) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long totalSessions = sessionRepository.findByUserIdOrderByStartedAtDesc(userId).size();
        double totalVolume = sessionRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream().mapToDouble(s -> s.getTotalVolumeKg().doubleValue()).sum();
        long sessionsWeek = sessionRepository.countByUserIdSince(userId, weekAgo);
        Double volumeWeek = sessionRepository.totalVolumeByUserIdSince(userId, weekAgo);
        return StatsDto.builder()
                .totalSessions(totalSessions).totalVolumeKg(totalVolume)
                .sessionsThisWeek(sessionsWeek)
                .volumeThisWeek(volumeWeek != null ? volumeWeek : 0.0)
                .currentStreak(calculateStreak(userId)).build();
    }

    public List<VolumeDataPoint> getVolumeByWeek(UUID userId, int weeks) {
        LocalDateTime from = LocalDateTime.now().minusWeeks(weeks);
        List<WorkoutSession> sessions = sessionRepository.findByUserIdSince(userId, from);
        Map<String, List<WorkoutSession>> byWeek = sessions.stream()
                .collect(Collectors.groupingBy(s -> getWeekKey(s.getStartedAt())));
        return byWeek.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> VolumeDataPoint.builder()
                        .week(e.getKey())
                        .volumeKg(e.getValue().stream().mapToDouble(s -> s.getTotalVolumeKg().doubleValue()).sum())
                        .sessions(e.getValue().size()).build())
                .collect(Collectors.toList());
    }

    public List<ExerciseProgressPoint> getExerciseProgress(UUID userId, UUID exerciseId) {
        List<ExerciseLog> logs = exerciseLogRepository.findByUserIdAndExerciseId(userId, exerciseId);
        return logs.stream().map(el -> {
            Optional<SetLog> bestSet = el.getSetLogs().stream()
                    .filter(SetLog::getIsCompleted)
                    .max(Comparator.comparing(s -> s.getWeightKg().doubleValue()));
            BigDecimal maxW = bestSet.map(SetLog::getWeightKg).orElse(BigDecimal.ZERO);
            int maxR = bestSet.map(SetLog::getReps).orElse(0);
            BigDecimal rm1 = maxW.multiply(BigDecimal.valueOf(1 + maxR / 30.0))
                    .setScale(2, RoundingMode.HALF_UP);
            return ExerciseProgressPoint.builder()
                    .date(el.getSession().getStartedAt())
                    .maxWeightKg(maxW).maxReps(maxR).estimated1rm(rm1).build();
        }).sorted(Comparator.comparing(ExerciseProgressPoint::getDate))
                .collect(Collectors.toList());
    }

    public List<PersonalRecordDto> getPersonalRecords(UUID userId) {
        return prRepository.findByUserIdOrderByWeightKgDesc(userId).stream()
                .map(pr -> PersonalRecordDto.builder()
                        .exerciseId(pr.getExercise().getId())
                        .exerciseName(pr.getExercise().getName())
                        .muscleGroup(pr.getExercise().getMuscleGroup())
                        .weightKg(pr.getWeightKg()).reps(pr.getReps())
                        .estimated1rm(pr.getEstimated1rm()).achievedAt(pr.getAchievedAt()).build())
                .collect(Collectors.toList());
    }

    public List<InsightDto> getInsights(UUID userId) {
        List<InsightDto> insights = new ArrayList<>();
        List<WorkoutSession> recent = sessionRepository.findByUserIdSince(userId, LocalDateTime.now().minusWeeks(4));

        if (recent.size() >= 4) {
            // Volume trend
            double recentAvg = recent.stream().limit(3).mapToDouble(s -> s.getTotalVolumeKg().doubleValue()).average().orElse(0);
            double oldAvg = recent.stream().skip(3).limit(3).mapToDouble(s -> s.getTotalVolumeKg().doubleValue()).average().orElse(0);
            if (oldAvg > 0) {
                double change = (recentAvg - oldAvg) / oldAvg * 100;
                if (change > 5) {
                    insights.add(InsightDto.builder().type("IMPROVEMENT")
                            .message(String.format("Volume in aumento del %.0f%% rispetto alle ultime sessioni!", change)).build());
                } else if (change < -10) {
                    insights.add(InsightDto.builder().type("VOLUME_DROP")
                            .message(String.format("Volume in calo del %.0f%%. Considera un deload o rivedi il piano.", Math.abs(change))).build());
                }
            }

            // Fatigue detection (RIR trend)
            double avgRir = recent.stream().limit(3)
                    .flatMap(s -> s.getExerciseLogs().stream())
                    .flatMap(el -> el.getSetLogs().stream())
                    .mapToInt(SetLog::getRir).average().orElse(2);
            if (avgRir < 1.0) {
                insights.add(InsightDto.builder().type("DELOAD_SUGGESTION")
                        .message("RIR medio molto basso nelle ultime sessioni. Potrebbe essere il momento di un deload.").build());
            }
        }

        // PR insights
        prRepository.findByUserIdOrderByWeightKgDesc(userId).stream()
                .filter(pr -> pr.getAchievedAt().isAfter(LocalDateTime.now().minusDays(7)))
                .findFirst()
                .ifPresent(pr -> insights.add(InsightDto.builder().type("PR")
                        .message("Nuovo Personal Record in " + pr.getExercise().getName() +
                                ": " + pr.getWeightKg() + "kg × " + pr.getReps())
                        .exerciseName(pr.getExercise().getName()).build()));

        return insights;
    }

    public LoadSuggestionDto getSuggestedLoad(UUID userId, UUID exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Esercizio non trovato"));
        List<ExerciseLog> logs = exerciseLogRepository.findByUserIdAndExerciseId(userId, exerciseId);
        if (logs.isEmpty()) {
            return LoadSuggestionDto.builder().exerciseId(exerciseId)
                    .exerciseName(exercise.getName())
                    .suggestedWeightKg(BigDecimal.ZERO)
                    .reason("Nessun dato precedente disponibile").build();
        }
        ExerciseLog lastLog = logs.get(0);
        List<SetLog> completedSets = lastLog.getSetLogs().stream()
                .filter(SetLog::getIsCompleted).collect(Collectors.toList());
        if (completedSets.isEmpty()) {
            return LoadSuggestionDto.builder().exerciseId(exerciseId)
                    .exerciseName(exercise.getName())
                    .suggestedWeightKg(BigDecimal.ZERO)
                    .reason("Nessuna serie completata nell'ultima sessione").build();
        }
        double avgRir = completedSets.stream().mapToInt(SetLog::getRir).average().orElse(2);
        BigDecimal lastWeight = completedSets.get(0).getWeightKg();
        BigDecimal suggested;
        String reason;
        if (avgRir <= 1) {
            suggested = lastWeight.add(BigDecimal.valueOf(2.5));
            reason = String.format("RIR medio %.1f: progressive overload, aumenta di 2.5kg", avgRir);
        } else if (avgRir >= 4) {
            suggested = lastWeight.subtract(BigDecimal.valueOf(2.5));
            reason = String.format("RIR medio %.1f: troppo facile, aumenta il peso", avgRir);
        } else {
            suggested = lastWeight;
            reason = String.format("RIR medio %.1f: mantieni il peso attuale", avgRir);
        }
        int maxReps = completedSets.stream().mapToInt(SetLog::getReps).max().orElse(10);
        BigDecimal rm1 = lastWeight.multiply(BigDecimal.valueOf(1 + maxReps / 30.0))
                .setScale(2, RoundingMode.HALF_UP);
        return LoadSuggestionDto.builder()
                .exerciseId(exerciseId).exerciseName(exercise.getName())
                .suggestedWeightKg(suggested).reason(reason).estimated1rm(rm1).build();
    }

    private int calculateStreak(UUID userId) {
        List<WorkoutSession> sessions = sessionRepository.findByUserIdOrderByStartedAtDesc(userId);
        int streak = 0;
        LocalDate day = LocalDate.now();
        for (WorkoutSession s : sessions) {
            LocalDate sessionDate = s.getStartedAt().toLocalDate();
            if (sessionDate.equals(day) || (streak == 0 && sessionDate.equals(day.minusDays(1)))) {
                streak++;
                day = sessionDate.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private String getWeekKey(LocalDateTime dt) {
        WeekFields wf = WeekFields.of(Locale.ITALY);
        int week = dt.get(wf.weekOfYear());
        return dt.getYear() + "-W" + String.format("%02d", week);
    }
}
