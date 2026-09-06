package edu.meialua.entity;

import edu.meialua.enums.Action;
import edu.meialua.enums.EntityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agregação por instância (entity + entityId + action), ao contrário do
 * AnalyticsMetric que agrega só por entity+action e por isso não sabe dizer
 * "qual" brinquedo/usuário. Responde perguntas tipo "produtos mais
 * vendidos/editados" (entity=PRODUCT) e "usuários mais ativos"
 * (entity=USER, action=LOGIN). category/brand/totalValue só fazem sentido
 * para entity=PRODUCT com action=SALE — ficam null/0 nos demais casos.
 */
@Entity
@Table(
        name = "analytics_entity_metrics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_entity_entityid_action",
                        columnNames = {"entity", "entity_id", "action"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEntityMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // VARCHAR explícito — ver comentário em AnalyticsMetric sobre por que não
    // deixar o Hibernate usar ENUM nativo do MySQL aqui.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private EntityType entity;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private Action action;

    private String category;

    private String brand;

    @Column(nullable = false)
    private Long totalCount;

    @Column(nullable = false)
    private Double totalValue;

    private LocalDateTime lastEventAt;
}
