package com.esun.financialsystem.data.model;

import java.time.LocalDateTime;

public record User(
        String userId,
        String userName,
        String email,
        String account,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
