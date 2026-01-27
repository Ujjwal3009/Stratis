package com.upsc.ai.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter implements Filter {

    private static final String REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Generate and set request ID
        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID, requestId);
        httpResponse.setHeader("X-Request-ID", requestId);

        // Wrap request and response for logging
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        try {
            // Log request
            logRequest(requestWrapper, requestId);

            // Continue with the filter chain
            chain.doFilter(requestWrapper, responseWrapper);

            // Log response
            logResponse(responseWrapper, requestId, System.currentTimeMillis() - startTime);

            // Copy response body to actual response
            responseWrapper.copyBodyToResponse();

        } finally {
            MDC.remove(REQUEST_ID);
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String requestId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;

        log.info("Incoming Request [{}] {} {} from {}",
                requestId, method, fullUrl, request.getRemoteAddr());

        // Log headers (excluding sensitive ones)
        log.debug("Request Headers [{}]:", requestId);
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            if (!isSensitiveHeader(headerName)) {
                log.debug("  {}: {}", headerName, request.getHeader(headerName));
            }
        });
    }

    private void logResponse(ContentCachingResponseWrapper response, String requestId, long duration) {
        int status = response.getStatus();

        log.info("Outgoing Response [{}] Status: {} - Duration: {}ms",
                requestId, status, duration);

        if (status >= 400) {
            log.warn("Request [{}] failed with status {}", requestId, status);
        }
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseName = headerName.toLowerCase();
        return lowerCaseName.contains("authorization")
                || lowerCaseName.contains("password")
                || lowerCaseName.contains("token")
                || lowerCaseName.contains("secret")
                || lowerCaseName.contains("api-key");
    }
}
