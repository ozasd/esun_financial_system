package com.esun.financialsystem.presentation.response;

import java.time.LocalDateTime;

public record UserResponse(
        String userId,
        String userName,
        String email,
        String account,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
