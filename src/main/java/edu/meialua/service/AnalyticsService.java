package edu.meialua.service;

import edu.meialua.dto.ActionAnalyticsDTO;
import edu.meialua.dto.EntityAnalyticsDTO;
import edu.meialua.dto.LogEvent;
import edu.meialua.entity.AnalyticsMetric;
import edu.meialua.enums.Action;
import edu.meialua.enums.EntityType;
import edu.meialua.repository.AnalyticsMetricRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


@ApplicationScoped
public class AnalyticsService {
    private final AnalyticsMetricRepository analyticsMetricRepository;

    @Inject
    public AnalyticsService(AnalyticsMetricRepository analyticsMetricRepository) {
        this.analyticsMetricRepository = analyticsMetricRepository;
    }

    @Transactional
    public void process(LogEvent logEvent) {

        AnalyticsMetric metric = analyticsMetricRepository.findByEntityAndAction(
                logEvent.getEntity(),
                logEvent.getAction()
        )
                .orElseGet(() -> AnalyticsMetric.builder()
                        .entity(logEvent.getEntity())
                        .action(logEvent.getAction())
                        .totalCount(0L)
                        .build()
                );


        metric.setTotalCount(metric.getTotalCount() + 1);
        metric.setLastEventAt(logEvent.getTimestamp());

        analyticsMetricRepository.persist(metric);


    }

    public long getTotalCount(){
        return analyticsMetricRepository.getTotalEvents();
    }

    public long getTotalByEntity(EntityType entity){
        return analyticsMetricRepository.getTotalByEntity(entity);
    }

    public long getTotalByAction(Action action){
        return analyticsMetricRepository.getTotalByAction(action);
    }

    public double getPercentageByEntity(EntityType entity) {
        long total = getTotalCount();
        long entityTotal = getTotalByEntity(entity);

        if (total == 0) {
            return 0.0;
        }

        return (entityTotal * 100.0) / total;
    }

    public EntityAnalyticsDTO getEntityAnalytics() {

        long total = getTotalCount();

        Map<EntityType, Double> percentages = new EnumMap<>(EntityType.class);

        for (EntityType entity : EntityType.values()) {

            long entityTotal = getTotalByEntity(entity);

            double percentage = total == 0
                    ? 0.0
                    : (entityTotal * 100.0) / total;

            percentages.put(entity, percentage);
        }

        return new EntityAnalyticsDTO(
                total,
                percentages
        );
    }

    public ActionAnalyticsDTO getActionAnalytics() {

        long total = getTotalCount();

        Map<Action, Long> totals = new EnumMap<>(Action.class);

        List<AnalyticsMetric> results =
                analyticsMetricRepository.getTotalsByAction();

        for (AnalyticsMetric metric : results) {

            totals.merge(
                    metric.getAction(),
                    metric.getTotalCount(),
                    Long::sum
            );
        }

        Map<Action, Double> percentages = new EnumMap<>(Action.class);

        for (Map.Entry<Action, Long> entry : totals.entrySet()) {

            double percentage = total == 0
                    ? 0.0
                    : (entry.getValue() * 100.0) / total;

            percentages.put(entry.getKey(), percentage);
        }

        return new ActionAnalyticsDTO(
                total,
                percentages
        );
    }
}