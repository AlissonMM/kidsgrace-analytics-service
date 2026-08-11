package edu.meialua.kafka.consumer;

import edu.meialua.dto.LogEvent;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class UserAnalyticsConsumer {

    @Incoming("user-analytics")
    @Blocking
    public void consume(LogEvent userLogEvent) {

        System.out.println("User Event Received:");
        System.out.println(userLogEvent);
    }
}