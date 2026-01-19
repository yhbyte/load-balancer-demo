package org.havryliuk.loadbalancer.registry;

import lombok.extern.slf4j.Slf4j;
import org.havryliuk.loadbalancer.config.LoadBalancerProperties;
import org.havryliuk.loadbalancer.model.BackendServer;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class ServerRegistry {

    private final ConcurrentMap<String, BackendServer> servers = new ConcurrentHashMap<>();

    public ServerRegistry(LoadBalancerProperties properties) {
        properties.servers().forEach(config -> {
            servers.put(config.id(), new BackendServer(config.id(), config.url(), config.weight()));
            log.info("Registered server: {} -> {} (weight={})", config.id(), config.url(), config.weight());
        });
    }

    public Collection<BackendServer> getAllServers() {
        return servers.values();
    }

    /**
     * Updates server health status and logs state transitions.
     */
    public void updateHealth(String serverId, boolean healthy) {
        BackendServer server = servers.get(serverId);
        if (server == null) {
            return;
        }

        boolean wasHealthy = server.isHealthy();
        server.setHealthy(healthy);

        if (wasHealthy != healthy) {
            log.info("Server {} is now {}", serverId, healthy ? "HEALTHY" : "UNHEALTHY");
        }
    }
}
