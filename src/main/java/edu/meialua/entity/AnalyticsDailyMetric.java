package edu.meialua.entity;

import edu.meialua.enums.Action;
import edu.meialua.enums.EntityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Rollup diário de eventos — mesma chave de agregação do AnalyticsMetric
 * (entity + action), mais a dimensão de tempo (day). Mantido em paralelo ao
 * AnalyticsMetric (que continua servindo o total acumulado "desde sempre")
 * para permitir séries temporais e filtros de período sem quebrar os
 * endpoints já existentes.
 */
@Entity
@Table(
        name = "analytics_daily_metrics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_entity_action_day",
                        columnNames = {"entity", "action", "day"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDailyMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // VARCHAR explícito — ver comentário em AnalyticsMetric sobre por que não
    // deixar o Hibernate usar ENUM nativo do MySQL aqui.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private EntityType entity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private Action action;

    @Column(nullable = false)
    private LocalDate day;

    @Column(nullable = false)
    private Long totalCount;
}
