package edu.meialua.dto;

import edu.meialua.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class EntityAnalyticsDTO {

    private long total;

    private Map<EntityType, Double> entities;
}