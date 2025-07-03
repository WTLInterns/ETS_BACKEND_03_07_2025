package com.example.demo.Service.Location;

import com.example.demo.Service.Location.LocationValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LocationValidationMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(LocationValidationMonitoringService.class);

    @Autowired
    private LocationValidationService locationValidationService;

    private final AtomicLong totalValidations = new AtomicLong(0);
    private final AtomicLong successfulValidations = new AtomicLong(0);
    private final AtomicLong failedValidations = new AtomicLong(0);
    private final AtomicLong geocodingCalls = new AtomicLong(0);
    private final AtomicLong geocodingFailures = new AtomicLong(0);

    // Performance metrics
    private final List<Long> validationTimes = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, AtomicInteger> errorCodeCounts = new HashMap<>();
    private final Map<String, AtomicInteger> validationMethodCounts = new HashMap<>();

    /**
     * Known test locations for Pune (for health checks)
     */
    private static final String[] TEST_LOCATIONS = {
            "Deccan, Pune",
            "Kharadi, Pune",
            "Hinjewadi, Pune",
            "Hadapsar, Pune",
            "Magarpatta, Pune",
            "Baner, Pune",
            "Aundh, Pune",
            "Katraj, Pune"
    };

    /**
     * Periodic health check every 30 minutes
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void performHealthCheck() {
        logger.info("Starting scheduled location validation health check...");

        try {
            int passed = 0;
            int total = TEST_LOCATIONS.length;

            for (String location : TEST_LOCATIONS) {
                try {
                    LocationValidationResult result = locationValidationService.validateLocation(location, "health-check");
                    if (result.isValid()) {
                        passed++;
                    } else {
                        logger.warn("Health check failed for location '{}': {}", location, result.getErrorMessage());
                    }
                } catch (Exception e) {
                    logger.error("Health check error for location '{}': {}", location, e.getMessage());
                }
            }

            double successRate = (double) passed / total * 100;
            logger.info("Health check completed: {}/{} locations passed ({}%)", passed, total, String.format("%.1f", successRate));

            if (successRate < 80) {
                logger.error("❌ ALERT: Location validation health check below threshold! Success rate: {}%", String.format("%.1f", successRate));
            } else {
                logger.info("✅ Location validation service health check passed");
            }

        } catch (Exception e) {
            logger.error("❌ CRITICAL: Health check system failure: {}", e.getMessage(), e);
        }
    }

    /**
     * Record validation metrics
     */
    public void recordValidation(LocationValidationResult result, long processingTimeMs) {
        totalValidations.incrementAndGet();

        if (result.isValid()) {
            successfulValidations.incrementAndGet();

            // Record validation method
            String method = result.getValidationMethod();
            if (method != null) {
                validationMethodCounts.computeIfAbsent(method, k -> new AtomicInteger(0)).incrementAndGet();
            }
        } else {
            failedValidations.incrementAndGet();

            // Record error codes
            String errorCode = result.getErrorCode();
            if (errorCode != null) {
                errorCodeCounts.computeIfAbsent(errorCode, k -> new AtomicInteger(0)).incrementAndGet();
            }
        }

        // Record processing time
        validationTimes.add(processingTimeMs);

        // Keep only last 1000 times for performance
        if (validationTimes.size() > 1000) {
            validationTimes.removeIf(time -> validationTimes.indexOf(time) < validationTimes.size() - 1000);
        }
    }

    /**
     * Record geocoding metrics
     */
    public void recordGeocodingCall(boolean success) {
        geocodingCalls.incrementAndGet();
        if (!success) {
            geocodingFailures.incrementAndGet();
        }
    }

    /**
     * Get comprehensive metrics
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Basic counts
        metrics.put("totalValidations", totalValidations.get());
        metrics.put("successfulValidations", successfulValidations.get());
        metrics.put("failedValidations", failedValidations.get());

        // Success rate
        long total = totalValidations.get();
        if (total > 0) {
            double successRate = (double) successfulValidations.get() / total * 100;
            metrics.put("successRate", String.format("%.2f%%", successRate));
        } else {
            metrics.put("successRate", "N/A");
        }

        // Geocoding metrics
        metrics.put("geocodingCalls", geocodingCalls.get());
        metrics.put("geocodingFailures", geocodingFailures.get());

        long geocodingTotal = geocodingCalls.get();
        if (geocodingTotal > 0) {
            double geocodingSuccessRate = (double) (geocodingTotal - geocodingFailures.get()) / geocodingTotal * 100;
            metrics.put("geocodingSuccessRate", String.format("%.2f%%", geocodingSuccessRate));
        } else {
            metrics.put("geocodingSuccessRate", "N/A");
        }

        // Performance metrics
        if (!validationTimes.isEmpty()) {
            List<Long> times = new ArrayList<>(validationTimes);
            times.sort(Long::compareTo);

            long min = times.get(0);
            long max = times.get(times.size() - 1);
            long avg = (long) times.stream().mapToLong(Long::longValue).average().orElse(0);
            long p95 = times.get((int) (times.size() * 0.95));

            Map<String, Object> performance = new HashMap<>();
            performance.put("minTimeMs", min);
            performance.put("maxTimeMs", max);
            performance.put("avgTimeMs", avg);
            performance.put("p95TimeMs", p95);
            performance.put("sampleSize", times.size());

            metrics.put("performance", performance);
        }

        // Error breakdown
        Map<String, Integer> errorBreakdown = new HashMap<>();
        errorCodeCounts.forEach((code, count) -> errorBreakdown.put(code, count.get()));
        metrics.put("errorBreakdown", errorBreakdown);

        // Validation method breakdown
        Map<String, Integer> methodBreakdown = new HashMap<>();
        validationMethodCounts.forEach((method, count) -> methodBreakdown.put(method, count.get()));
        metrics.put("validationMethodBreakdown", methodBreakdown);

        // Cache stats
        metrics.put("cacheStats", locationValidationService.getCacheStats());

        // Timestamp
        metrics.put("generatedAt", LocalDateTime.now());

        return metrics;
    }

    /**
     * Reset all metrics (for maintenance)
     */
    public void resetMetrics() {
        totalValidations.set(0);
        successfulValidations.set(0);
        failedValidations.set(0);
        geocodingCalls.set(0);
        geocodingFailures.set(0);
        validationTimes.clear();
        errorCodeCounts.clear();
        validationMethodCounts.clear();

        logger.info("All validation metrics have been reset");
    }

    /**
     * Get system status
     */
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            // Basic service health
            boolean serviceHealthy = locationValidationService.isServiceHealthy();
            status.put("serviceHealth", serviceHealthy ? "UP" : "DOWN");

            // Quick validation test
            long startTime = System.currentTimeMillis();
            LocationValidationResult testResult = locationValidationService.validateLocation("Deccan, Pune", "status-check");
            long responseTime = System.currentTimeMillis() - startTime;

            status.put("testValidation", testResult.isValid() ? "PASS" : "FAIL");
            status.put("responseTimeMs", responseTime);

            // Current metrics summary
            long total = totalValidations.get();
            if (total > 0) {
                double successRate = (double) successfulValidations.get() / total * 100;
                status.put("overallSuccessRate", String.format("%.1f%%", successRate));
            }

            status.put("status", serviceHealthy && testResult.isValid() ? "HEALTHY" : "DEGRADED");

        } catch (Exception e) {
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
            logger.error("Error getting system status: {}", e.getMessage());
        }

        return status;
    }
}
@Service
class MonitoredLocationValidationService {

    private static final Logger logger = LoggerFactory.getLogger(MonitoredLocationValidationService.class);

    @Autowired
    private LocationValidationService locationValidationService;

    @Autowired
    private LocationValidationMonitoringService monitoringService;

    /**
     * Validated location with full monitoring
     */
    public LocationValidationResult validateLocationWithMonitoring(String location, String locationType) {
        long startTime = System.currentTimeMillis();

        try {
            LocationValidationResult result = locationValidationService.validateLocation(location, locationType);

            long processingTime = System.currentTimeMillis() - startTime;
            monitoringService.recordValidation(result, processingTime);

            logger.debug("Location validation completed in {}ms - Location: '{}', Valid: {}, Method: '{}'",
                    processingTime, location, result.isValid(), result.getValidationMethod());

            return result;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            LocationValidationResult errorResult = LocationValidationResult.error(
                    "VALIDATION_EXCEPTION",
                    "Validation failed: " + e.getMessage()
            );

            monitoringService.recordValidation(errorResult, processingTime);
            logger.error("Location validation exception in {}ms - Location: '{}', Error: {}",
                    processingTime, location, e.getMessage());

            return errorResult;
        }
    }
}