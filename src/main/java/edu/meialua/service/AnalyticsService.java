package edu.meialua.service;

import edu.meialua.dto.LogEvent;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnalyticsService {

        public void process(LogEvent logEvent) {

            System.out.println("Processing analytics event: ");
            System.out.println(logEvent);
        }
}
