package edu.meialua.repository;

import edu.meialua.entity.AnalyticsMetric;
import edu.meialua.enums.Action;
import edu.meialua.enums.EntityType;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.find;

@ApplicationScoped
public class AnalyticsMetricRepository implements PanacheRepository<AnalyticsMetric> {

    public Optional<AnalyticsMetric> findByEntityAndAction(
            EntityType entity,
            Action action
    ) {
        return find(
                "entity = ?1 and action = ?2",
                entity,
                action
        ).firstResultOptional();
    }


    public long getTotalEvents(){
        return find(
                "select coalesce(sum(totalCount), 0) from AnalyticsMetric"
        )
                .project(Long.class)
                .firstResult();
    }

    public long getTotalByEntity(EntityType entity) {
        return find(
                "select coalesce(sum(totalCount), 0) " +
                        "from AnalyticsMetric " +
                        "where entity = ?1",
                entity
        )
                .project(Long.class)
                .firstResult();
    }

    public long getTotalByAction(Action action) {
        return find(
                "select coalesce(sum(totalCount), 0) " +
                        "from AnalyticsMetric " +
                        "where action = ?1",
                action
        )
                .project(Long.class)
                .firstResult();
    }
}