package org.havryliuk.resourceserver.dto;

import java.time.Instant;

public record ServerInfoResponse(
        String instanceId,
        String serverName,
        int port,
        Instant timestamp
) {
}
