package com.fitdiary.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    String accessToken;
    String refreshToken;
    String tokenType;
    UserDto user;
}
