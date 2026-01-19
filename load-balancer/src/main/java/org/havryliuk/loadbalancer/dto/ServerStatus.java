package org.havryliuk.loadbalancer.dto;

public record ServerStatus(
        String id,
        String url,
        int weight,
        boolean healthy,
        long requestCount
) {
}
