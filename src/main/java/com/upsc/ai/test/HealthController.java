package com.upsc.ai.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public String check() {
        log.info("Health check endpoint called");
        log.debug("Returning health status");
        return "UPSC AI Backend is running, boss! 🚀";
    }
}
