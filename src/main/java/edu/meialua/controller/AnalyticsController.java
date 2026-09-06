package edu.meialua.controller;

import edu.meialua.dto.ActionAnalyticsDTO;
import edu.meialua.dto.CategoryRevenueDTO;
import edu.meialua.dto.EntityAnalyticsDTO;
import edu.meialua.dto.EventCountDTO;
import edu.meialua.dto.LoginStatsDTO;
import edu.meialua.dto.TimeSeriesPointDTO;
import edu.meialua.dto.TopEntityDTO;
import edu.meialua.enums.Action;
import edu.meialua.enums.EntityType;
import edu.meialua.service.AnalyticsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDate;
import java.util.List;

@Path("/analytics")
@Produces(MediaType.APPLICATION_JSON)
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Inject
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // Todos os endpoints "desde sempre" abaixo aceitam opcionalmente
    // ?from=AAAA-MM-DD&to=AAAA-MM-DD — quando ambos são informados, a
    // consulta passa a considerar só o período; sem eles, comportamento
    // inalterado (total acumulado).

    @GET
    @Path("/events")
    public EventCountDTO getTotalEvents(
            @QueryParam("from") String from,
            @QueryParam("to") String to
    ) {
        long total = hasRange(from, to)
                ? analyticsService.getTotalCount(parseDate(from), parseDate(to))
                : analyticsService.getTotalCount();

        return new EventCountDTO(total);
    }

    @GET
    @Path("/entities")
    public EntityAnalyticsDTO getEntityAnalytics(
            @QueryParam("from") String from,
            @QueryParam("to") String to
    ) {
        return hasRange(from, to)
                ? analyticsService.getEntityAnalytics(parseDate(from), parseDate(to))
                : analyticsService.getEntityAnalytics();
    }

    @GET
    @Path("/entities/{entity}")
    public long getTotalByEntity(
            @PathParam("entity") EntityType entity,
            @QueryParam("from") String from,
            @QueryParam("to") String to
    ) {
        return hasRange(from, to)
                ? analyticsService.getTotalByEntity(entity, parseDate(from), parseDate(to))
                : analyticsService.getTotalByEntity(entity);
    }

    @GET
    @Path("/actions/{action}")
    public long getTotalByAction(
            @PathParam("action") Action action,
            @QueryParam("from") String from,
            @QueryParam("to") String to
    ) {
        return hasRange(from, to)
                ? analyticsService.getTotalByAction(action, parseDate(from), parseDate(to))
                : analyticsService.getTotalByAction(action);
    }

    @GET
    @Path("/actions")
    public ActionAnalyticsDTO getActionAnalytics() {
        return analyticsService.getActionAnalytics();
    }

    /**
     * Série temporal diária — ex.: GET /analytics/timeseries?entity=USER&action=LOGIN&from=2026-08-01&to=2026-08-31
     * para um gráfico de "logins por dia".
     */
    @GET
    @Path("/timeseries")
    public List<TimeSeriesPointDTO> getTimeSeries(
            @QueryParam("entity") EntityType entity,
            @QueryParam("action") Action action,
            @QueryParam("from") String from,
            @QueryParam("to") String to
    ) {
        if (entity == null || action == null || from == null || to == null) {
            throw new BadRequestException(
                    "Parâmetros obrigatórios: entity, action, from, to (from/to no formato AAAA-MM-DD)."
            );
        }

        return analyticsService.getTimeSeries(entity, action, parseDate(from), parseDate(to));
    }

    /**
     * Top-N por instância — ex.: GET /analytics/top?entity=PRODUCT&action=SALE&limit=10&orderBy=value
     * para "produtos mais vendidos" (por quantidade ou por receita).
     */
    @GET
    @Path("/top")
    public List<TopEntityDTO> getTopEntities(
            @QueryParam("entity") EntityType entity,
            @QueryParam("action") Action action,
            @QueryParam("limit") @DefaultValue("10") int limit,
            @QueryParam("orderBy") @DefaultValue("count") String orderBy
    ) {
        if (entity == null || action == null) {
            throw new BadRequestException("Parâmetros obrigatórios: entity, action.");
        }

        return analyticsService.getTopEntities(entity, action, limit, "value".equalsIgnoreCase(orderBy));
    }

    @GET
    @Path("/revenue-by-category")
    public List<CategoryRevenueDTO> getRevenueByCategory() {
        return analyticsService.getRevenueByCategory();
    }

    @GET
    @Path("/login-stats")
    public LoginStatsDTO getLoginStats() {
        return analyticsService.getLoginStats();
    }

    private boolean hasRange(String from, String to) {
        return from != null && to != null;
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            throw new BadRequestException("Data inválida: \"" + value + "\" — use o formato AAAA-MM-DD.");
        }
    }
}
