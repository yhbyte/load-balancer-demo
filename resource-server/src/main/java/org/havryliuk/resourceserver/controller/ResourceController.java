package org.havryliuk.resourceserver.controller;

import lombok.RequiredArgsConstructor;
import org.havryliuk.resourceserver.config.ResourceServerProperties;
import org.havryliuk.resourceserver.dto.ServerInfoResponse;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceServerProperties properties;
    private final Environment environment;

    @GetMapping("/resource")
    public ServerInfoResponse getResource() {
        return new ServerInfoResponse(
                properties.id(),
                properties.name(),
                Integer.parseInt(environment.getProperty("server.port", "8080")),
                Instant.now()
        );
    }
}
