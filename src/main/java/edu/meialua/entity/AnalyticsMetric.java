package edu.meialua.entity;


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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityType entity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityAction action;

    @Column(nullable = false)
    private Long totalCount;

    private LocalDateTime lastEventAt;
}
