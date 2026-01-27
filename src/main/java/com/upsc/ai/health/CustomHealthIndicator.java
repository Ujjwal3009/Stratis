package com.upsc.ai.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Check disk space
            File root = new File("/");
            long usableSpace = root.getUsableSpace();
            long totalSpace = root.getTotalSpace();
            double usagePercentage = ((double) (totalSpace - usableSpace) / totalSpace) * 100;

            log.debug("Disk usage: {}/{} bytes ({}%)",
                    totalSpace - usableSpace, totalSpace, String.format("%.2f", usagePercentage));

            if (usagePercentage > 90) {
                return Health.down()
                        .withDetail("disk_usage", String.format("%.2f%%", usagePercentage))
                        .withDetail("reason", "Disk space critically low")
                        .build();
            }

            if (usagePercentage > 80) {
                return Health.up()
                        .withDetail("disk_usage", String.format("%.2f%%", usagePercentage))
                        .withDetail("warning", "Disk space running low")
                        .build();
            }

            return Health.up()
                    .withDetail("disk_usage", String.format("%.2f%%", usagePercentage))
                    .withDetail("status", "All systems operational")
                    .build();

        } catch (Exception e) {
            log.error("Health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
