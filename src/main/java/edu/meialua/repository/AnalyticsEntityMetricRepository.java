package edu.meialua.repository;

import edu.meialua.entity.AnalyticsEntityMetric;
import edu.meialua.enums.Action;
import edu.meialua.enums.EntityType;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AnalyticsEntityMetricRepository implements PanacheRepository<AnalyticsEntityMetric> {

    public Optional<AnalyticsEntityMetric> findByEntityAndEntityIdAndAction(
            EntityType entity,
            Long entityId,
            Action action
    ) {
        return find(
                "entity = ?1 and entityId = ?2 and action = ?3",
                entity,
                entityId,
                action
        ).firstResultOptional();
    }

    public List<AnalyticsEntityMetric> topByCount(EntityType entity, Action action, int limit) {
        return find(
                "entity = ?1 and action = ?2 order by totalCount desc",
                entity,
                action
        ).page(0, limit).list();
    }

    public List<AnalyticsEntityMetric> topByValue(EntityType entity, Action action, int limit) {
        return find(
                "entity = ?1 and action = ?2 order by totalValue desc",
                entity,
                action
        ).page(0, limit).list();
    }

    /**
     * Receita somada por categoria — só faz sentido para entity=PRODUCT,
     * action=SALE (onde category/totalValue são de fato preenchidos).
     */
    public List<Object[]> sumValueGroupByCategory(EntityType entity, Action action) {
        return getEntityManager()
                .createQuery(
                        "select m.category, sum(m.totalValue) " +
                                "from AnalyticsEntityMetric m " +
                                "where m.entity = ?1 and m.action = ?2 and m.category is not null " +
                                "group by m.category " +
                                "order by sum(m.totalValue) desc",
                        Object[].class
                )
                .setParameter(1, entity)
                .setParameter(2, action)
                .getResultList();
    }
}
