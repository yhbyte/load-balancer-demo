package org.havryliuk.loadbalancer.dto;

import java.util.List;

public record LoadBalancerStats(
        int totalServers,
        int healthyServers,
        long totalRequests,
        List<ServerStatus> servers
) {
}
