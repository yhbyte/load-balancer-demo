package org.havryliuk.loadbalancer.model;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;

/**
 * Represents a backend server in the load balancer pool. Thread-safe for concurrent health checks and request routing.
 */
public class BackendServer {

    @Getter
    private final String id;
    @Getter
    private final String url;
    @Getter
    private final int weight;

    private final AtomicBoolean healthy = new AtomicBoolean(true);
    private final AtomicInteger currentWeight = new AtomicInteger(0);
    private final AtomicLong requestCount = new AtomicLong(0);

    public BackendServer(String id, String url, int weight) {
        this.id = id;
        this.url = url;
        this.weight = weight;
    }

    public boolean isHealthy() {
        return healthy.get();
    }

    public void setHealthy(boolean value) {
        healthy.set(value);
    }

    public int getCurrentWeight() {
        return currentWeight.get();
    }

    public void addCurrentWeight(int delta) {
        currentWeight.addAndGet(delta);
    }

    public long getRequestCount() {
        return requestCount.get();
    }

    public void incrementRequestCount() {
        requestCount.incrementAndGet();
    }

    public String getHealthCheckUrl() {
        return url + "/actuator/health";
    }
}
