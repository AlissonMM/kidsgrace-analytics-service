package edu.meialua.controller;

import edu.meialua.dto.EventCountDTO;
import edu.meialua.service.AnalyticsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@Path("/analytics")
@Produces(MediaType.APPLICATION_JSON)
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Inject
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GET
    @Path("/events")
    public EventCountDTO getTotalEvents() {
        return new EventCountDTO(
                analyticsService.getTotalEvents()
        );
    }
}
