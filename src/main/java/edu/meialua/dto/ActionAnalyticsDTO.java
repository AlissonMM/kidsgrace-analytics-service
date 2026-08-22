package edu.meialua.dto;

import edu.meialua.enums.Action;

import java.util.Map;

public record ActionAnalyticsDTO(
        long total,
        Map<Action, Double> actions
) {
}
