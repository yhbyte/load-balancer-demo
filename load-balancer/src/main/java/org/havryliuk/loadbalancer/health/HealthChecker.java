package org.havryliuk.loadbalancer.health;

import lombok.extern.slf4j.Slf4j;
import org.havryliuk.loadbalancer.model.BackendServer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class HealthChecker {

    private static final String HEALTH_UP_INDICATOR = "\"status\":\"UP\"";

    private final RestClient restClient = RestClient.create();

    public boolean isHealthy(BackendServer server) {
        try {
            String response = restClient.get()
                    .uri(server.getHealthCheckUrl())
                    .retrieve()
                    .body(String.class);

            return response != null && response.contains(HEALTH_UP_INDICATOR);
        } catch (Exception e) {
            log.debug("Health check failed for {}: {}", server.getId(), e.getMessage());
            return false;
        }
    }
}
