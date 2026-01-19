package org.havryliuk.loadbalancer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "loadbalancer")
public record LoadBalancerProperties(List<ServerConfig> servers) {

    public record ServerConfig(String id, String url, int weight) {

        public ServerConfig {
            if (weight < 1) {
                weight = 1;
            }
        }
    }

    public LoadBalancerProperties {
        if (servers == null) {
            servers = List.of();
        }
    }
}
