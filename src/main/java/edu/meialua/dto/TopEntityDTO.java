package edu.meialua.dto;

public record TopEntityDTO(
        Long entityId,
        String category,
        String brand,
        long totalCount,
        double totalValue
) {
}
