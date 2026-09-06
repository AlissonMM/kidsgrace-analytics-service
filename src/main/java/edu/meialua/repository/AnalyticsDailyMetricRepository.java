package edu.meialua.repository;

import edu.meialua.entity.AnalyticsDailyMetric;
import edu.meialua.enums.Action;
import edu.meialua.enums.EntityType;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AnalyticsDailyMetricRepository implements PanacheRepository<AnalyticsDailyMetric> {

    public Optional<AnalyticsDailyMetric> findByEntityAndActionAndDay(
            EntityType entity,
            Action action,
            LocalDate day
    ) {
        return find(
                "entity = ?1 and action = ?2 and day = ?3",
                entity,
                action,
                day
        ).firstResultOptional();
    }

    public List<AnalyticsDailyMetric> findSeries(
            EntityType entity,
            Action action,
            LocalDate from,
            LocalDate to
    ) {
        return find(
                "entity = ?1 and action = ?2 and day between ?3 and ?4 order by day",
                entity,
                action,
                from,
                to
        ).list();
    }

    public long sumByEntityAndActionAndRange(EntityType entity, Action action, LocalDate from, LocalDate to) {
        return find(
                "select coalesce(sum(totalCount), 0) " +
                        "from AnalyticsDailyMetric " +
                        "where entity = ?1 and action = ?2 and day between ?3 and ?4",
                entity,
                action,
                from,
                to
        ).project(Long.class).firstResult();
    }

    public long sumByActionAndRange(Action action, LocalDate from, LocalDate to) {
        return find(
                "select coalesce(sum(totalCount), 0) " +
                        "from AnalyticsDailyMetric " +
                        "where action = ?1 and day between ?2 and ?3",
                action,
                from,
                to
        ).project(Long.class).firstResult();
    }

    public long sumByEntityAndRange(EntityType entity, LocalDate from, LocalDate to) {
        return find(
                "select coalesce(sum(totalCount), 0) " +
                        "from AnalyticsDailyMetric " +
                        "where entity = ?1 and day between ?2 and ?3",
                entity,
                from,
                to
        ).project(Long.class).firstResult();
    }

    public long sumByRange(LocalDate from, LocalDate to) {
        return find(
                "select coalesce(sum(totalCount), 0) " +
                        "from AnalyticsDailyMetric " +
                        "where day between ?1 and ?2",
                from,
                to
        ).project(Long.class).firstResult();
    }
}
