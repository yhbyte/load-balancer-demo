package org.havryliuk.loadbalancer.algorithm;

import org.havryliuk.loadbalancer.model.BackendServer;

import java.util.List;
import java.util.Optional;

public interface LoadBalancingAlgorithm {
    Optional<BackendServer> selectServer(List<BackendServer> servers);
}
