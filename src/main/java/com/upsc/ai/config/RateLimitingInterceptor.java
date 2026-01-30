package com.upsc.ai.config;

import com.upsc.ai.security.UserPrincipal;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitingConfig rateLimitingConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Only rate limit AI generation endpoints
        if (request.getRequestURI().contains("/generate") || request.getRequestURI().contains("/parse")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
                Bucket bucket = rateLimitingConfig.resolveBucket(principal.getId());
                ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

                if (probe.isConsumed()) {
                    response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                    return true;
                } else {
                    long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
                    response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
                    response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                            "Rate limit exceeded. Try again in " + waitForRefill + " seconds.");
                    return false;
                }
            }
        }
        return true;
    }
}
