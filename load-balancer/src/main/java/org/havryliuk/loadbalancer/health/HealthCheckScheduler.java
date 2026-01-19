package org.havryliuk.loadbalancer.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.havryliuk.loadbalancer.model.BackendServer;
import org.havryliuk.loadbalancer.registry.ServerRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthCheckScheduler {

    private final ServerRegistry serverRegistry;
    private final HealthChecker healthChecker;

    // Virtual threads for parallel health checks
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Scheduled(fixedDelayString = "${loadbalancer.health-check.interval:5000}")
    public void checkAll() {
        log.debug("Running health checks");
        serverRegistry.getAllServers().forEach(server ->
                executor.submit(() -> checkServer(server))
        );
    }

    private void checkServer(BackendServer server) {
        boolean healthy = healthChecker.isHealthy(server);
        serverRegistry.updateHealth(server.getId(), healthy);
    }
}
