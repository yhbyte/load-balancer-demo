package org.havryliuk.loadbalancer.controller;

import lombok.RequiredArgsConstructor;
import org.havryliuk.loadbalancer.dto.LoadBalancerStats;
import org.havryliuk.loadbalancer.dto.ServerStatus;
import org.havryliuk.loadbalancer.model.BackendServer;
import org.havryliuk.loadbalancer.registry.ServerRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lb")
@RequiredArgsConstructor
public class AdminController {

    private final ServerRegistry serverRegistry;

    @GetMapping("/stats")
    public LoadBalancerStats getStats() {
        List<ServerStatus> servers = getServerStatuses();
        return new LoadBalancerStats(
                servers.size(),
                (int) servers.stream().filter(ServerStatus::healthy).count(),
                servers.stream().mapToLong(ServerStatus::requestCount).sum(),
                servers
        );
    }

    @GetMapping("/servers")
    public List<ServerStatus> getServers() {
        return getServerStatuses();
    }

    private List<ServerStatus> getServerStatuses() {
        return serverRegistry.getAllServers().stream()
                .map(AdminController::toStatus)
                .toList();
    }

    private static ServerStatus toStatus(BackendServer server) {
        return new ServerStatus(
                server.getId(),
                server.getUrl(),
                server.getWeight(),
                server.isHealthy(),
                server.getRequestCount()
        );
    }
}
