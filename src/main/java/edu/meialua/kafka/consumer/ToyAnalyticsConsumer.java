package edu.meialua.kafka.consumer;

import edu.meialua.dto.LogEvent;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class ToyAnalyticsConsumer {

    @Incoming("toy-analytics")
    @Blocking
    public void consume(LogEvent toyLogEvent) {

        System.out.println("Toy Event Received:");
        System.out.println(toyLogEvent);
    }
}