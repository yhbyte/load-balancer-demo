package org.havryliuk.loadbalancer.algorithm;

import org.havryliuk.loadbalancer.model.BackendServer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Smooth Weighted Round Robin algorithm (Nginx-style).
 *
 * Distributes requests smoothly based on weights. A server with weight 3
 * receives 3x more requests than one with weight 1.
 *
 * Algorithm per request:
 * 1. Add each server's weight to its currentWeight
 * 2. Select server with highest currentWeight
 * 3. Subtract totalWeight from selected server's currentWeight
 *
 * For weights [3,2,1] produces: A,B,A,C,A,B (smooth) instead of A,A,A,B,B,C (bursty).
 */
@Component
public class WeightedRoundRobinAlgorithm implements LoadBalancingAlgorithm {

    @Override
    public Optional<BackendServer> selectServer(List<BackendServer> servers) {
        if (servers == null || servers.isEmpty()) {
            return Optional.empty();
        }

        List<BackendServer> healthy = servers.stream()
                .filter(BackendServer::isHealthy)
                .toList();

        if (healthy.isEmpty()) {
            return Optional.empty();
        }

        BackendServer selected = selectByWeight(healthy);
        if (selected != null) {
            selected.incrementRequestCount();
        }
        return Optional.ofNullable(selected);
    }

    /**
     * Thread-safe selection using smooth weighted round-robin.
     */
    private synchronized BackendServer selectByWeight(List<BackendServer> servers) {
        if (servers.size() == 1) {
            return servers.getFirst();
        }

        int totalWeight = 0;
        BackendServer selected = null;
        int maxCurrentWeight = Integer.MIN_VALUE;

        for (BackendServer server : servers) {
            totalWeight += server.getWeight();
            server.addCurrentWeight(server.getWeight());

            if (server.getCurrentWeight() > maxCurrentWeight) {
                maxCurrentWeight = server.getCurrentWeight();
                selected = server;
            }
        }

        if (selected != null) {
            selected.addCurrentWeight(-totalWeight);
        }

        return selected;
    }
}
