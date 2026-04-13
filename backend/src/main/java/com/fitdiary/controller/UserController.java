package com.fitdiary.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitdiary.dto.ApiResponse;
import com.fitdiary.dto.UpdateProfileRequest;
import com.fitdiary.dto.UserDto;
import com.fitdiary.entity.User;
import com.fitdiary.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "Gestione profilo utente")
class UserController {
    private final UserRepository userRepository;

    @GetMapping("/me")
    @Operation(summary = "Ottieni profilo utente corrente")
    public ResponseEntity<ApiResponse<UserDto>> getMe(@AuthenticationPrincipal UserDetails ud) {
        User user = userRepository.findById(UUID.fromString(ud.getUsername()))
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
        return ResponseEntity.ok(ApiResponse.ok(UserDto.from(user)));
    }

    @PutMapping("/me")
    @Operation(summary = "Aggiorna profilo utente")
    public ResponseEntity<ApiResponse<UserDto>> updateMe(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody UpdateProfileRequest req) {
        User user = userRepository.findById(UUID.fromString(ud.getUsername()))
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
        if (req.getName() != null) user.setName(req.getName());
        if (req.getAge() != null) user.setAge(req.getAge());
        if (req.getWeightKg() != null) user.setWeightKg(req.getWeightKg());
        if (req.getHeightCm() != null) user.setHeightCm(req.getHeightCm());
        if (req.getGoal() != null) user.setGoal(req.getGoal());
        if (req.getLevel() != null) {
            try { user.setLevel(User.FitnessLevel.valueOf(req.getLevel().toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }
        return ResponseEntity.ok(ApiResponse.ok(UserDto.from(userRepository.save(user))));
    }
}
