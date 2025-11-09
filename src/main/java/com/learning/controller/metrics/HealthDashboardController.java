package com.learning.controller.metrics;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/identity/health")
public class HealthDashboardController {

    private final HealthEndpoint healthEndpoint;

    public HealthDashboardController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    /**
     * Get comprehensive health status
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getHealthStatus(){
        HealthComponent health = healthEndpoint.health();
        Map<String, Object> healthStatus = new LinkedHashMap<>();
        healthStatus.put("status", health.getStatus().getCode());
        healthStatus.put("components", extractComponents(health));
        healthStatus.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(healthStatus);
    }

    private Map<String, Object> extractComponents(HealthComponent health) {
        Map<String, Object> components = new LinkedHashMap<>();

        if (health instanceof org.springframework.boot.actuate.health.CompositeHealth) {
            org.springframework.boot.actuate.health.CompositeHealth composite =
                    (org.springframework.boot.actuate.health.CompositeHealth) health;

            composite.getComponents().forEach((name, component) -> {
                Map<String, Object> componentInfo = new LinkedHashMap<>();
                componentInfo.put("status", component.getStatus().getCode());

//                if (component.getDetails() != null) {
//                    componentInfo.put("details", component.getDetails());
//                }

                components.put(name, componentInfo);
            });
        }

        return components;
    }
}

