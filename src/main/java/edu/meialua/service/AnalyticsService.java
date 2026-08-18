package edu.meialua.service;

import edu.meialua.dto.LogEvent;
import edu.meialua.entity.AnalyticsMetric;
import edu.meialua.enums.Action;
import edu.meialua.enums.EntityType;
import edu.meialua.repository.AnalyticsMetricRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.concurrent.atomic.AtomicLong;

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
}