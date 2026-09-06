package edu.meialua.service;

import edu.meialua.dto.ActionAnalyticsDTO;
import edu.meialua.dto.CategoryRevenueDTO;
import edu.meialua.dto.EntityAnalyticsDTO;
import edu.meialua.dto.LogEvent;
import edu.meialua.dto.LoginStatsDTO;
import edu.meialua.dto.TimeSeriesPointDTO;
import edu.meialua.dto.TopEntityDTO;
import edu.meialua.entity.AnalyticsDailyMetric;
import edu.meialua.entity.AnalyticsEntityMetric;
import edu.meialua.entity.AnalyticsMetric;
import edu.meialua.enums.Action;
import edu.meialua.enums.EntityType;
import edu.meialua.repository.AnalyticsDailyMetricRepository;
import edu.meialua.repository.AnalyticsEntityMetricRepository;
import edu.meialua.repository.AnalyticsMetricRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@ApplicationScoped
public class AnalyticsService {
    private final AnalyticsMetricRepository analyticsMetricRepository;
    private final AnalyticsDailyMetricRepository analyticsDailyMetricRepository;
    private final AnalyticsEntityMetricRepository analyticsEntityMetricRepository;

    @Inject
    public AnalyticsService(
            AnalyticsMetricRepository analyticsMetricRepository,
            AnalyticsDailyMetricRepository analyticsDailyMetricRepository,
            AnalyticsEntityMetricRepository analyticsEntityMetricRepository
    ) {
        this.analyticsMetricRepository = analyticsMetricRepository;
        this.analyticsDailyMetricRepository = analyticsDailyMetricRepository;
        this.analyticsEntityMetricRepository = analyticsEntityMetricRepository;
    }

    @Transactional
    public void process(LogEvent logEvent) {
        incrementOverallMetric(logEvent);
        incrementDailyMetric(logEvent);
        incrementEntityMetric(logEvent);
    }

    private void incrementOverallMetric(LogEvent logEvent) {
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

    private void incrementDailyMetric(LogEvent logEvent) {
        LocalDate day = logEvent.getTimestamp().toLocalDate();

        AnalyticsDailyMetric metric = analyticsDailyMetricRepository
                .findByEntityAndActionAndDay(logEvent.getEntity(), logEvent.getAction(), day)
                .orElseGet(() -> AnalyticsDailyMetric.builder()
                        .entity(logEvent.getEntity())
                        .action(logEvent.getAction())
                        .day(day)
                        .totalCount(0L)
                        .build()
                );

        metric.setTotalCount(metric.getTotalCount() + 1);

        analyticsDailyMetricRepository.persist(metric);
    }

    private void incrementEntityMetric(LogEvent logEvent) {
        // Eventos sem entityId (não deveria acontecer hoje, mas por segurança)
        // não têm "instância" pra agregar aqui.
        if (logEvent.getEntityId() == null) {
            return;
        }

        AnalyticsEntityMetric metric = analyticsEntityMetricRepository
                .findByEntityAndEntityIdAndAction(logEvent.getEntity(), logEvent.getEntityId(), logEvent.getAction())
                .orElseGet(() -> AnalyticsEntityMetric.builder()
                        .entity(logEvent.getEntity())
                        .entityId(logEvent.getEntityId())
                        .action(logEvent.getAction())
                        .totalCount(0L)
                        .totalValue(0.0)
                        .build()
                );

        metric.setTotalCount(metric.getTotalCount() + 1);
        metric.setLastEventAt(logEvent.getTimestamp());

        if (logEvent.getCategory() != null) {
            metric.setCategory(logEvent.getCategory());
        }
        if (logEvent.getBrand() != null) {
            metric.setBrand(logEvent.getBrand());
        }
        if (logEvent.getUnitValue() != null && logEvent.getQuantity() != null) {
            double soldValue = logEvent.getUnitValue() * logEvent.getQuantity();
            metric.setTotalValue(metric.getTotalValue() + soldValue);
        }

        analyticsEntityMetricRepository.persist(metric);
    }

    // ------------------------------------------------------------------
    // Consultas "desde sempre" — comportamento já existente, inalterado.
    // ------------------------------------------------------------------

    public long getTotalCount() {
        return analyticsMetricRepository.getTotalEvents();
    }

    public long getTotalByEntity(EntityType entity) {
        return analyticsMetricRepository.getTotalByEntity(entity);
    }

    public long getTotalByAction(Action action) {
        return analyticsMetricRepository.getTotalByAction(action);
    }

    public EntityAnalyticsDTO getEntityAnalytics() {
        long total = getTotalCount();

        Map<EntityType, Double> percentages = new EnumMap<>(EntityType.class);

        for (EntityType entity : EntityType.values()) {
            long entityTotal = getTotalByEntity(entity);
            percentages.put(entity, percentage(entityTotal, total));
        }

        return new EntityAnalyticsDTO(total, percentages);
    }

    /**
     * Percentual de eventos por action — corrigido para agregar via GROUP BY
     * no banco (AnalyticsMetricRepository.sumTotalCountGroupByAction) em vez
     * de trazer todas as linhas e somar em Java.
     */
    public ActionAnalyticsDTO getActionAnalytics() {
        long total = getTotalCount();

        Map<Action, Double> percentages = new EnumMap<>(Action.class);

        for (Object[] row : analyticsMetricRepository.sumTotalCountGroupByAction()) {
            Action action = (Action) row[0];
            long actionTotal = (Long) row[1];
            percentages.put(action, percentage(actionTotal, total));
        }

        return new ActionAnalyticsDTO(total, percentages);
    }

    // ------------------------------------------------------------------
    // Consultas com filtro de período (from/to sempre não-nulos aqui — o
    // controller decide cair para as versões "desde sempre" acima quando
    // o cliente não informa período).
    // ------------------------------------------------------------------

    public long getTotalCount(LocalDate from, LocalDate to) {
        return analyticsDailyMetricRepository.sumByRange(from, to);
    }

    public long getTotalByEntity(EntityType entity, LocalDate from, LocalDate to) {
        return analyticsDailyMetricRepository.sumByEntityAndRange(entity, from, to);
    }

    public long getTotalByAction(Action action, LocalDate from, LocalDate to) {
        return analyticsDailyMetricRepository.sumByActionAndRange(action, from, to);
    }

    public EntityAnalyticsDTO getEntityAnalytics(LocalDate from, LocalDate to) {
        long total = getTotalCount(from, to);

        Map<EntityType, Double> percentages = new EnumMap<>(EntityType.class);

        for (EntityType entity : EntityType.values()) {
            long entityTotal = getTotalByEntity(entity, from, to);
            percentages.put(entity, percentage(entityTotal, total));
        }

        return new EntityAnalyticsDTO(total, percentages);
    }

    /**
     * Série temporal diária (com dias sem evento preenchidos como zero, para
     * o gráfico de linha não ter "buracos"), ex.: "logins por dia nos
     * últimos 30 dias".
     */
    public List<TimeSeriesPointDTO> getTimeSeries(EntityType entity, Action action, LocalDate from, LocalDate to) {
        Map<LocalDate, Long> byDay = new LinkedHashMap<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            byDay.put(day, 0L);
        }

        for (AnalyticsDailyMetric metric : analyticsDailyMetricRepository.findSeries(entity, action, from, to)) {
            byDay.put(metric.getDay(), metric.getTotalCount());
        }

        List<TimeSeriesPointDTO> series = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> entry : byDay.entrySet()) {
            series.add(new TimeSeriesPointDTO(entry.getKey(), entry.getValue()));
        }

        return series;
    }

    /**
     * Top-N por instância (entity+entityId), ex.: produtos mais
     * vendidos/editados (entity=PRODUCT) ou usuários mais ativos em login
     * (entity=USER, action=LOGIN).
     */
    public List<TopEntityDTO> getTopEntities(EntityType entity, Action action, int limit, boolean orderByValue) {
        List<AnalyticsEntityMetric> metrics = orderByValue
                ? analyticsEntityMetricRepository.topByValue(entity, action, limit)
                : analyticsEntityMetricRepository.topByCount(entity, action, limit);

        List<TopEntityDTO> result = new ArrayList<>();
        for (AnalyticsEntityMetric metric : metrics) {
            result.add(new TopEntityDTO(
                    metric.getEntityId(),
                    metric.getCategory(),
                    metric.getBrand(),
                    metric.getTotalCount(),
                    round2(metric.getTotalValue())
            ));
        }

        return result;
    }

    /**
     * Receita somada por categoria (só considera vendas confirmadas —
     * Action.SALE é publicado no momento do pagamento, não no checkout).
     */
    public List<CategoryRevenueDTO> getRevenueByCategory() {
        List<CategoryRevenueDTO> result = new ArrayList<>();

        for (Object[] row : analyticsEntityMetricRepository.sumValueGroupByCategory(EntityType.PRODUCT, Action.SALE)) {
            String category = (String) row[0];
            double totalRevenue = (Double) row[1];
            result.add(new CategoryRevenueDTO(category, round2(totalRevenue)));
        }

        return result;
    }

    /**
     * Taxa de sucesso de login — LOGIN vs LOGIN_FAILED, desde sempre.
     */
    public LoginStatsDTO getLoginStats() {
        long successCount = getTotalByAction(Action.LOGIN);
        long failureCount = getTotalByAction(Action.LOGIN_FAILED);
        long totalAttempts = successCount + failureCount;

        double successRate = totalAttempts == 0
                ? 0.0
                : (successCount * 100.0) / totalAttempts;

        return new LoginStatsDTO(successCount, failureCount, totalAttempts, successRate);
    }

    private double percentage(long part, long total) {
        return total == 0 ? 0.0 : (part * 100.0) / total;
    }

    // Evita ruído de conversão float -> double (ex.: 49.900001525878906) no
    // JSON de saída — os valores de origem (unitValue) vêm como float da API.
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
