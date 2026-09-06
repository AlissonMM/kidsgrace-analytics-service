package edu.meialua.dto;

public record LoginStatsDTO(
        long successCount,
        long failureCount,
        long totalAttempts,
        double successRate
) {
}
