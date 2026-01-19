package org.havryliuk.resourceserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resource-server.instance")
public record ResourceServerProperties(String id, String name) {

    public ResourceServerProperties {
        if (id == null || id.isBlank()) {
            id = "unknown";
        }
        if (name == null || name.isBlank()) {
            name = "Resource Server";
        }
    }
}
