package com.fitdiary.service;

import com.fitdiary.dto.*;
import com.fitdiary.entity.*;
import com.fitdiary.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkoutService {

    private final WorkoutPlanRepository planRepository;
    private final WorkoutDayRepository dayRepository;
    private final PlannedExerciseRepository plannedExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutSessionRepository sessionRepository;
    private final ExerciseLogRepository exerciseLogRepository;
    private final SetLogRepository setLogRepository;
    private final PersonalRecordRepository prRepository;
    private final UserRepository userRepository;

    // ─── PLANS ───────────────────────────────────────────────────────────────

    public List<WorkoutPlanDto> getPlans(UUID userId) {
        return planRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toPlanDto).collect(Collectors.toList());
    }

    public WorkoutPlanDto createPlan(UUID userId, CreatePlanRequest req) {
        User user = userRepository.getReferenceById(userId);
        WorkoutPlan plan = WorkoutPlan.builder()
                .user(user).name(req.getName()).split(req.getSplit())
                .daysPerWeek(req.getDaysPerWeek() != null ? req.getDaysPerWeek() : 3)
                .goal(req.getGoal()).build();
        planRepository.save(plan);

        if (req.getWorkoutDays() != null) {
            int order = 0;
            for (CreateWorkoutDayRequest dayReq : req.getWorkoutDays()) {
                WorkoutDay day = WorkoutDay.builder()
                        .plan(plan).name(dayReq.getName()).dayOrder(order++).build();
                dayRepository.save(day);
                if (dayReq.getExercises() != null) {
                    int exOrder = 0;
                    for (CreatePlannedExerciseRequest exReq : dayReq.getExercises()) {
                        Exercise ex = exerciseRepository.findById(exReq.getExerciseId())
                                .orElseThrow(() -> new IllegalArgumentException("Esercizio non trovato"));
                        PlannedExercise pe = PlannedExercise.builder()
                                .workoutDay(day).exercise(ex).exerciseOrder(exOrder++)
                                .sets(exReq.getSets() != null ? exReq.getSets() : 3)
                                .repsRange(exReq.getRepsRange() != null ? exReq.getRepsRange() : "8-12")
                                .restSeconds(exReq.getRestSeconds() != null ? exReq.getRestSeconds() : 90)
                                .targetWeightKg(exReq.getTargetWeightKg() != null ? exReq.getTargetWeightKg() : BigDecimal.ZERO)
                                .notes(exReq.getNotes()).build();
                        day.getPlannedExercises().add(pe);
                    }
                }
            }
        }
        return toPlanDto(plan);
    }

    public WorkoutPlanDto getPlan(UUID userId, UUID planId) {
        WorkoutPlan plan = planRepository.findById(planId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Scheda non trovata"));
        return toPlanDto(plan);
    }

    public void deletePlan(UUID userId, UUID planId) {
        WorkoutPlan plan = planRepository.findById(planId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Scheda non trovata"));
        planRepository.delete(plan);
    }

    public WorkoutPlanDto addExerciseToDay(UUID userId, UUID planId, UUID dayId, CreatePlannedExerciseRequest req) {
        WorkoutPlan plan = planRepository.findById(planId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Scheda non trovata"));
        WorkoutDay day = plan.getWorkoutDays().stream()
                .filter(d -> d.getId().equals(dayId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Giorno non trovato"));
        Exercise ex = exerciseRepository.findById(req.getExerciseId())
                .orElseThrow(() -> new IllegalArgumentException("Esercizio non trovato"));
        int nextOrder = day.getPlannedExercises().size();
        PlannedExercise pe = PlannedExercise.builder()
                .workoutDay(day).exercise(ex).exerciseOrder(nextOrder)
                .sets(req.getSets() != null ? req.getSets() : 3)
                .repsRange(req.getRepsRange() != null ? req.getRepsRange() : "8-12")
                .restSeconds(req.getRestSeconds() != null ? req.getRestSeconds() : 90)
                .targetWeightKg(req.getTargetWeightKg() != null ? req.getTargetWeightKg() : BigDecimal.ZERO)
                .notes(req.getNotes()).build();
        day.getPlannedExercises().add(pe);
        dayRepository.save(day);
        return toPlanDto(planRepository.findById(planId).get());
    }

    public WorkoutPlanDto updatePlannedExercise(UUID userId, UUID planId, UUID plannedExerciseId, UpdatePlannedExerciseRequest req) {
        WorkoutPlan plan = planRepository.findById(planId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Scheda non trovata"));
        PlannedExercise pe = plannedExerciseRepository.findById(plannedExerciseId)
                .filter(p -> p.getWorkoutDay().getPlan().getId().equals(plan.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Esercizio non trovato nella scheda"));
        if (req.getSets() != null) pe.setSets(req.getSets());
        if (req.getRepsRange() != null) pe.setRepsRange(req.getRepsRange());
        if (req.getRestSeconds() != null) pe.setRestSeconds(req.getRestSeconds());
        if (req.getTargetWeightKg() != null) pe.setTargetWeightKg(req.getTargetWeightKg());
        pe.setNotes(req.getNotes());
        plannedExerciseRepository.save(pe);
        return toPlanDto(planRepository.findById(planId).get());
    }

    public WorkoutPlanDto deletePlannedExercise(UUID userId, UUID planId, UUID plannedExerciseId) {
        WorkoutPlan plan = planRepository.findById(planId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Scheda non trovata"));
        PlannedExercise pe = plannedExerciseRepository.findById(plannedExerciseId)
                .filter(p -> p.getWorkoutDay().getPlan().getId().equals(plan.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Esercizio non trovato nella scheda"));
        WorkoutDay day = pe.getWorkoutDay();
        day.getPlannedExercises().remove(pe);
        plannedExerciseRepository.delete(pe);
        // reorder
        List<PlannedExercise> remaining = day.getPlannedExercises();
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setExerciseOrder(i);
            plannedExerciseRepository.save(remaining.get(i));
        }
        return toPlanDto(planRepository.findById(planId).get());
    }

    public WorkoutPlanDto setActivePlan(UUID userId, UUID planId) {
        planRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(p -> { p.setIsActive(false); planRepository.save(p); });
        WorkoutPlan plan = planRepository.findById(planId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Scheda non trovata"));
        plan.setIsActive(true);
        return toPlanDto(planRepository.save(plan));
    }

    // ─── SESSIONS ─────────────────────────────────────────────────────────────

    public WorkoutSessionDto startSession(UUID userId, StartSessionRequest req) {
        User user = userRepository.getReferenceById(userId);
        WorkoutPlan plan = req.getPlanId() != null ? planRepository.getReferenceById(req.getPlanId()) : null;
        WorkoutDay day = req.getWorkoutDayId() != null ? dayRepository.getReferenceById(req.getWorkoutDayId()) : null;
        WorkoutSession session = WorkoutSession.builder()
                .user(user).plan(plan).workoutDay(day)
                .dayName(req.getDayName())
                .startedAt(req.getStartedAt() != null ? req.getStartedAt() : LocalDateTime.now())
                .build();
        return toSessionDto(sessionRepository.save(session), false);
    }

    public WorkoutSessionDto finishSession(UUID userId, FinishSessionRequest req) {
        WorkoutSession session = sessionRepository.findById(req.getSessionId())
                .filter(s -> s.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Sessione non trovata"));

        session.setFinishedAt(req.getFinishedAt() != null ? req.getFinishedAt() : LocalDateTime.now());
        session.setNotes(req.getNotes());

        BigDecimal totalVolume = BigDecimal.ZERO;

        if (req.getExercises() != null) {
            int exOrder = 0;
            for (LoggedExerciseRequest exReq : req.getExercises()) {
                Exercise exercise = exerciseRepository.findById(exReq.getExerciseId())
                        .orElseThrow(() -> new IllegalArgumentException("Esercizio non trovato"));
                ExerciseLog exLog = ExerciseLog.builder()
                        .session(session).exercise(exercise)
                        .exerciseName(exReq.getExerciseName() != null ? exReq.getExerciseName() : exercise.getName())
                        .muscleGroup(exReq.getMuscleGroup() != null ? exReq.getMuscleGroup() : exercise.getMuscleGroup())
                        .exerciseOrder(exOrder++).build();

                if (exReq.getSets() != null) {
                    int setNum = 1;
                    for (LoggedSetRequest setReq : exReq.getSets()) {
                        SetLog setLog = SetLog.builder()
                                .exerciseLog(exLog).setNumber(setNum++)
                                .weightKg(setReq.getWeightKg()).reps(setReq.getReps())
                                .rir(setReq.getRir() != null ? setReq.getRir() : 2)
                                .durationSeconds(setReq.getDurationSeconds())
                                .isCompleted(setReq.getIsCompleted() == null || setReq.getIsCompleted())
                                .notes(setReq.getNotes()).build();
                        exLog.getSetLogs().add(setLog);
                        totalVolume = totalVolume.add(setReq.getWeightKg().multiply(BigDecimal.valueOf(setReq.getReps())));
                    }
                    // Check PR
                    checkAndUpdatePR(userId, session, exercise, exReq.getSets());
                }
                session.getExerciseLogs().add(exLog);
            }
        }

        long durationSeconds = java.time.Duration.between(session.getStartedAt(), session.getFinishedAt()).getSeconds();
        session.setDurationSeconds((int) durationSeconds);
        session.setTotalVolumeKg(totalVolume);

        return toSessionDto(sessionRepository.save(session), true);
    }

    private void checkAndUpdatePR(UUID userId, WorkoutSession session, Exercise exercise, List<LoggedSetRequest> sets) {
        sets.stream()
                .filter(s -> s.getIsCompleted() == null || s.getIsCompleted())
                .max(Comparator.comparing(s -> s.getWeightKg().doubleValue() * (1 + s.getReps() / 30.0)))
                .ifPresent(best -> {
                    BigDecimal rm1 = best.getWeightKg().multiply(
                            BigDecimal.valueOf(1 + best.getReps() / 30.0));
                    Optional<PersonalRecord> existing = prRepository.findByUserIdAndExerciseId(userId, exercise.getId());
                    boolean isNewPR = existing.isEmpty() ||
                            rm1.compareTo(existing.get().getEstimated1rm()) > 0;
                    if (isNewPR) {
                        PersonalRecord pr = existing.orElseGet(() -> PersonalRecord.builder()
                                .user(userRepository.getReferenceById(userId))
                                .exercise(exercise).build());
                        pr.setWeightKg(best.getWeightKg());
                        pr.setReps(best.getReps());
                        pr.setEstimated1rm(rm1);
                        pr.setAchievedAt(LocalDateTime.now());
                        pr.setSession(session);
                        prRepository.save(pr);
                    }
                });
    }

    public List<WorkoutSessionDto> getSessions(UUID userId, int limit) {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream().limit(limit).map(s -> toSessionDto(s, false)).collect(Collectors.toList());
    }

    public WorkoutSessionDto getSession(UUID userId, UUID sessionId) {
        WorkoutSession s = sessionRepository.findById(sessionId)
                .filter(sess -> sess.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Sessione non trovata"));
        return toSessionDto(s, true);
    }

    public AddSetResponse addSet(UUID userId, AddSetRequest req) {
        WorkoutSession session = sessionRepository.findById(req.getSessionId())
                .filter(s -> s.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Sessione non trovata"));
        ExerciseLog exLog = exerciseLogRepository.findById(req.getExerciseLogId())
                .orElseThrow(() -> new IllegalArgumentException("Exercise log non trovato"));
        int setNum = req.getSetNumber() != null ? req.getSetNumber() :
                exLog.getSetLogs().size() + 1;
        SetLog setLog = SetLog.builder()
                .exerciseLog(exLog).setNumber(setNum)
                .weightKg(req.getWeightKg()).reps(req.getReps())
                .rir(req.getRir() != null ? req.getRir() : 2)
                .durationSeconds(req.getDurationSeconds())
                .isCompleted(req.getIsCompleted() == null || req.getIsCompleted())
                .notes(req.getNotes()).build();
        setLogRepository.save(setLog);
        return AddSetResponse.builder()
                .setId(setLog.getId()).setNumber(setLog.getSetNumber())
                .weightKg(setLog.getWeightKg()).reps(setLog.getReps()).build();
    }

    // ─── EXERCISES ────────────────────────────────────────────────────────────

    public List<ExerciseDto> getExercises(UUID userId, String search) {
        List<Exercise> exercises = search != null && !search.isBlank()
                ? exerciseRepository.findByNameContainingIgnoreCaseAndIsCustomFalse(search)
                : exerciseRepository.findAvailableForUser(userId);
        return exercises.stream().map(this::toExerciseDto).collect(Collectors.toList());
    }

    public ExerciseDto createCustomExercise(UUID userId, CreateExerciseRequest req) {
        User user = userRepository.getReferenceById(userId);
        Exercise ex = Exercise.builder()
                .name(req.getName()).muscleGroup(req.getMuscleGroup())
                .category(req.getCategory()).notes(req.getNotes())
                .isCustom(true).createdBy(user).build();
        return toExerciseDto(exerciseRepository.save(ex));
    }

    // ─── MAPPERS ──────────────────────────────────────────────────────────────

    private WorkoutPlanDto toPlanDto(WorkoutPlan plan) {
        return WorkoutPlanDto.builder()
                .id(plan.getId()).name(plan.getName()).split(plan.getSplit())
                .daysPerWeek(plan.getDaysPerWeek()).goal(plan.getGoal())
                .isActive(plan.getIsActive()).createdAt(plan.getCreatedAt())
                .workoutDays(plan.getWorkoutDays().stream().map(d -> WorkoutDayDto.builder()
                        .id(d.getId()).name(d.getName()).dayOrder(d.getDayOrder())
                        .exercises(d.getPlannedExercises().stream().map(pe -> PlannedExerciseDto.builder()
                                .id(pe.getId()).exerciseId(pe.getExercise().getId())
                                .exerciseName(pe.getExercise().getName())
                                .muscleGroup(pe.getExercise().getMuscleGroup())
                                .exerciseOrder(pe.getExerciseOrder()).sets(pe.getSets())
                                .repsRange(pe.getRepsRange()).restSeconds(pe.getRestSeconds())
                                .targetWeightKg(pe.getTargetWeightKg()).notes(pe.getNotes())
                                .exerciseNotes(pe.getExercise().getNotes()).build())
                                .collect(Collectors.toList())).build())
                        .collect(Collectors.toList())).build();
    }

    private WorkoutSessionDto toSessionDto(WorkoutSession s, boolean includeLogs) {
        var builder = WorkoutSessionDto.builder()
                .id(s.getId())
                .planId(s.getPlan() != null ? s.getPlan().getId() : null)
                .planName(s.getPlan() != null ? s.getPlan().getName() : null)
                .dayName(s.getDayName()).startedAt(s.getStartedAt())
                .finishedAt(s.getFinishedAt()).durationSeconds(s.getDurationSeconds())
                .totalVolumeKg(s.getTotalVolumeKg()).notes(s.getNotes());
        if (includeLogs) {
            builder.exerciseLogs(s.getExerciseLogs().stream().map(el -> ExerciseLogDto.builder()
                    .id(el.getId()).exerciseId(el.getExercise().getId())
                    .exerciseName(el.getExerciseName()).muscleGroup(el.getMuscleGroup())
                    .exerciseOrder(el.getExerciseOrder())
                    .sets(el.getSetLogs().stream().map(sl -> SetLogDto.builder()
                            .id(sl.getId()).setNumber(sl.getSetNumber()).weightKg(sl.getWeightKg())
                            .reps(sl.getReps()).rir(sl.getRir()).durationSeconds(sl.getDurationSeconds())
                            .isCompleted(sl.getIsCompleted()).notes(sl.getNotes()).build())
                            .collect(Collectors.toList())).build())
                    .collect(Collectors.toList()));
        }
        return builder.build();
    }

    private ExerciseDto toExerciseDto(Exercise e) {
        return ExerciseDto.builder().id(e.getId()).name(e.getName())
                .muscleGroup(e.getMuscleGroup()).category(e.getCategory())
                .isCustom(e.getIsCustom()).notes(e.getNotes()).build();
    }
}
