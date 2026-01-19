package org.havryliuk.loadbalancer.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.havryliuk.loadbalancer.service.LoadBalancerService;
import org.havryliuk.loadbalancer.service.RequestForwarder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoadBalancerController {

    private final LoadBalancerService loadBalancerService;
    private final RequestForwarder requestForwarder;

    @RequestMapping("/**")
    public ResponseEntity<byte[]> handleRequest(HttpServletRequest request,
                                                @RequestBody(required = false) byte[] body) {
        String path = request.getRequestURI();

        if (path.startsWith("/lb/") || path.startsWith("/actuator/")) {
            return ResponseEntity.notFound().build();
        }

        return requestForwarder.forward(request, loadBalancerService.selectServer(), body);
    }
}
