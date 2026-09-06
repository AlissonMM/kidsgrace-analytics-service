package edu.meialua.kafka.consumer;

import edu.meialua.dto.LogEvent;
import edu.meialua.service.AnalyticsService;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class OrderAnalyticsConsumer {

    private final AnalyticsService analyticsService;

    @Inject
    public OrderAnalyticsConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // Recebe tanto os eventos de pedido (ORDER_CREATED/PAID/CANCELLED,
    // entity=ORDER) quanto os eventos de venda por item (Action.SALE,
    // entity=PRODUCT) publicados no mesmo tópico orders.events.
    @Incoming("order-analytics")
    @Blocking
    public void consume(LogEvent orderLogEvent) {
        analyticsService.process(orderLogEvent);
    }
}
