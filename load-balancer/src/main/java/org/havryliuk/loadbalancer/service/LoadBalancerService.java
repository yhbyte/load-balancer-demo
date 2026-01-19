package org.havryliuk.loadbalancer.service;

import lombok.RequiredArgsConstructor;
import org.havryliuk.loadbalancer.algorithm.LoadBalancingAlgorithm;
import org.havryliuk.loadbalancer.exception.NoAvailableServerException;
import org.havryliuk.loadbalancer.model.BackendServer;
import org.havryliuk.loadbalancer.registry.ServerRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class LoadBalancerService {

    private final ServerRegistry serverRegistry;
    private final LoadBalancingAlgorithm algorithm;

    public BackendServer selectServer() {
        return algorithm.selectServer(new ArrayList<>(serverRegistry.getAllServers()))
                .orElseThrow(NoAvailableServerException::new);
    }
}
