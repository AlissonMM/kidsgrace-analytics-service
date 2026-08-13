package edu.meialua.service;

import edu.meialua.dto.LogEvent;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class AnalyticsService {

    private final AtomicLong totalEvents = new AtomicLong();

        public void process(LogEvent logEvent) {

            totalEvents.incrementAndGet();

            System.out.println("Processing analytics event: ");
            System.out.println(logEvent);
        }


        public long getTotalEvents() {
            return totalEvents.get();
        }
}
