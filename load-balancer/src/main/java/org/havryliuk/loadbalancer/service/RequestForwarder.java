package org.havryliuk.loadbalancer.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.havryliuk.loadbalancer.model.BackendServer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.Set;

@Slf4j
@Service
public class RequestForwarder {

    // HTTP hop-by-hop headers that should not be forwarded
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailers", "transfer-encoding", "upgrade", "host"
    );

    private final RestClient restClient = RestClient.create();

    public ResponseEntity<byte[]> forward(HttpServletRequest request, BackendServer server, byte[] body) {
        String targetUrl = buildTargetUrl(server.getUrl(), request);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        log.debug("Forwarding {} {} -> {}", method, request.getRequestURI(), server.getId());

        RestClient.RequestBodySpec spec = restClient.method(method).uri(targetUrl);
        copyRequestHeaders(request, spec);

        if (body != null && body.length > 0) {
            spec.body(body);
        }

        return spec.exchange((req, res) -> {
            HttpHeaders headers = filterResponseHeaders(res.getHeaders());
            byte[] responseBody = res.getBody().readAllBytes();
            return ResponseEntity.status(res.getStatusCode()).headers(headers).body(responseBody);
        });
    }

    private String buildTargetUrl(String baseUrl, HttpServletRequest request) {
        String query = request.getQueryString();
        return query != null
                ? baseUrl + request.getRequestURI() + "?" + query
                : baseUrl + request.getRequestURI();
    }

    private void copyRequestHeaders(HttpServletRequest request, RestClient.RequestBodySpec spec) {
        Collections.list(request.getHeaderNames()).stream()
                .filter(name -> !HOP_BY_HOP_HEADERS.contains(name.toLowerCase()))
                .forEach(name -> Collections.list(request.getHeaders(name))
                        .forEach(value -> spec.header(name, value)));
    }

    private HttpHeaders filterResponseHeaders(HttpHeaders source) {
        HttpHeaders filtered = new HttpHeaders();
        source.forEach((name, values) -> {
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                filtered.addAll(name, values);
            }
        });
        return filtered;
    }
}
