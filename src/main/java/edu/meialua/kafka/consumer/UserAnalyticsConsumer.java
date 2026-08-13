package edu.meialua.kafka.consumer;

import edu.meialua.dto.LogEvent;
import edu.meialua.service.AnalyticsService;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class UserAnalyticsConsumer {

    private final AnalyticsService analyticsService;

    @Inject
    public UserAnalyticsConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Incoming("user-analytics")
    @Blocking
    public void consume(LogEvent userLogEvent) {

        analyticsService.process(userLogEvent);
    }
}