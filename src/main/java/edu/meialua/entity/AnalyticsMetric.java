package edu.meialua.entity;


import edu.meialua.enums.Action;
import edu.meialua.enums.EntityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.action.internal.EntityAction;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "analytics_metrics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_entity_action",
                        columnNames = {"entity", "action"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // columnDefinition = VARCHAR força o Hibernate a não usar um ENUM nativo
    // do MySQL aqui: um enum nativo trava com "Data truncated for column"
    // toda vez que um valor novo é adicionado ao Action/EntityType do Java
    // sem regenerar a coluna manualmente.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private EntityType entity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private Action action;

    @Column(nullable = false)
    private Long totalCount;

    private LocalDateTime lastEventAt;
}
