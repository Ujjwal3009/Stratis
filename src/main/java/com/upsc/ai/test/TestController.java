package com.upsc.ai.test;

import com.upsc.ai.exception.BusinessException;
import com.upsc.ai.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/logging")
    public Map<String, String> testLogging() {
        log.trace("TRACE level log - very detailed");
        log.debug("DEBUG level log - debugging information");
        log.info("INFO level log - general information");
        log.warn("WARN level log - warning message");
        log.error("ERROR level log - error message");

        Map<String, String> response = new HashMap<>();
        response.put("message", "Check logs to see different log levels");
        response.put("status", "success");
        return response;
    }

    @GetMapping("/error/not-found")
    public void testNotFoundException() {
        log.info("Testing ResourceNotFoundException");
        throw new ResourceNotFoundException("Test Resource", "id", "123");
    }

    @GetMapping("/error/business")
    public void testBusinessException() {
        log.info("Testing BusinessException");
        throw new BusinessException("This is a test business exception", "TEST_ERROR");
    }

    @GetMapping("/error/illegal-argument")
    public void testIllegalArgumentException() {
        log.info("Testing IllegalArgumentException");
        throw new IllegalArgumentException("Invalid argument provided for testing");
    }

    @GetMapping("/error/generic")
    public void testGenericException() {
        log.info("Testing generic exception");
        throw new RuntimeException("This is a test runtime exception");
    }

    @GetMapping("/success")
    public Map<String, Object> testSuccess() {
        log.info("Testing successful response");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "This is a successful response");
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    @GetMapping("/slow")
    public Map<String, String> testSlowEndpoint() throws InterruptedException {
        log.info("Testing slow endpoint - simulating delay");
        Thread.sleep(2000); // 2 second delay

        Map<String, String> response = new HashMap<>();
        response.put("message", "This endpoint took 2 seconds to respond");
        response.put("status", "success");
        return response;
    }
}
