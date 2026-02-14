package com.opticstore.Analytics.controller;

import com.opticstore.Analytics.dto.AnalyticsRequestDTO;
import com.opticstore.Analytics.dto.CustomerAnalyticsDTO;
import com.opticstore.Analytics.dto.ProductPerformanceDTO;
import com.opticstore.Analytics.dto.RevenueAnalyticsDTO;
import com.opticstore.Analytics.model.AnalyticsFilter;
import com.opticstore.Analytics.model.TimePeriod;
import com.opticstore.Analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics and reporting endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get revenue analytics")
    public ResponseEntity<?> getRevenueAnalytics(
            @RequestParam(defaultValue = "THIS_MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(defaultValue = "false") boolean includeComparisons) {

        try {
            TimePeriod timePeriod = TimePeriod.valueOf(period);

            // Validate CUSTOM period
            if (timePeriod == TimePeriod.CUSTOM) {
                if (startDate == null || endDate == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "startDate and endDate are required for CUSTOM period",
                            "period", period,
                            "providedStartDate", startDate,
                            "providedEndDate", endDate
                    ));
                }

                // Validate date range (max 1 year for performance)
                long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
                if (daysBetween > 365) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Date range cannot exceed 365 days",
                            "daysRequested", daysBetween,
                            "maxAllowed", 365
                    ));
                }

                if (endDate.isBefore(startDate)) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "endDate cannot be before startDate",
                            "startDate", startDate,
                            "endDate", endDate
                    ));
                }
            }

            AnalyticsRequestDTO request = new AnalyticsRequestDTO();
            request.setPeriod(timePeriod);
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setCategory(category);
            request.setBrand(brand);
            request.setIncludeComparisons(includeComparisons);

            RevenueAnalyticsDTO analytics = analyticsService.getRevenueAnalytics(request);
            return ResponseEntity.ok(analytics);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid period value",
                    "validPeriods", Arrays.toString(TimePeriod.values()),
                    "provided", period
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/products/top")
    @Operation(summary = "Get top performing products")
    public ResponseEntity<?> getTopProducts(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "revenue") String sortBy) {

        try {
            AnalyticsFilter filter = new AnalyticsFilter();

            if (period != null) {
                filter.setPeriod(TimePeriod.valueOf(period));
            } else {
                filter.setPeriod(TimePeriod.THIS_MONTH);
            }

            filter.setStartDate(startDate);
            filter.setEndDate(endDate);
            filter.setCategory(category);
            filter.setBrand(brand);
            filter.setLimit(limit);
            filter.setSortBy(sortBy);

            // Validate CUSTOM period
            if (filter.getPeriod() == TimePeriod.CUSTOM) {
                if (startDate == null || endDate == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "startDate and endDate are required for CUSTOM period",
                            "period", period,
                            "providedStartDate", startDate,
                            "providedEndDate", endDate
                    ));
                }

                if (endDate.isBefore(startDate)) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "endDate cannot be before startDate",
                            "startDate", startDate,
                            "endDate", endDate
                    ));
                }
            }

            ProductPerformanceDTO performance = analyticsService.getProductPerformance(filter);
            return ResponseEntity.ok(performance);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid period value",
                    "validPeriods", Arrays.toString(TimePeriod.values()),
                    "provided", period
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch product analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/customers")
    @Operation(summary = "Get customer analytics")
    public ResponseEntity<?> getCustomerAnalytics(
            @RequestParam(defaultValue = "THIS_MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand) {

        try {
            TimePeriod timePeriod = TimePeriod.valueOf(period);

            AnalyticsFilter filter = new AnalyticsFilter();
            filter.setPeriod(timePeriod);
            filter.setStartDate(startDate);
            filter.setEndDate(endDate);
            filter.setCategory(category);
            filter.setBrand(brand);

            // Validate CUSTOM period
            if (timePeriod == TimePeriod.CUSTOM) {
                if (startDate == null || endDate == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "startDate and endDate are required for CUSTOM period",
                            "period", period,
                            "providedStartDate", startDate,
                            "providedEndDate", endDate
                    ));
                }

                if (endDate.isBefore(startDate)) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "endDate cannot be before startDate",
                            "startDate", startDate,
                            "endDate", endDate
                    ));
                }
            }

            CustomerAnalyticsDTO analytics = analyticsService.getCustomerAnalytics(filter);
            return ResponseEntity.ok(analytics);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid period value",
                    "validPeriods", Arrays.toString(TimePeriod.values()),
                    "provided", period
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch customer analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/revenue/by-category")
    @Operation(summary = "Get revenue breakdown by category")
    public ResponseEntity<?> getRevenueByCategory(
            @RequestParam(defaultValue = "THIS_MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String brand) {

        try {
            TimePeriod timePeriod = TimePeriod.valueOf(period);

            AnalyticsFilter filter = new AnalyticsFilter();
            filter.setPeriod(timePeriod);
            filter.setStartDate(startDate);
            filter.setEndDate(endDate);
            filter.setBrand(brand);

            var revenueByCategory = analyticsService.getRevenueByCategory(filter);
            return ResponseEntity.ok(revenueByCategory);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid period value",
                    "validPeriods", Arrays.toString(TimePeriod.values()),
                    "provided", period
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch revenue by category: " + e.getMessage()));
        }
    }

    @GetMapping("/revenue/by-brand")
    @Operation(summary = "Get revenue breakdown by brand")
    public ResponseEntity<?> getRevenueByBrand(
            @RequestParam(defaultValue = "THIS_MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category) {

        try {
            TimePeriod timePeriod = TimePeriod.valueOf(period);

            AnalyticsFilter filter = new AnalyticsFilter();
            filter.setPeriod(timePeriod);
            filter.setStartDate(startDate);
            filter.setEndDate(endDate);
            filter.setCategory(category);

            var revenueByBrand = analyticsService.getRevenueByBrand(filter);
            return ResponseEntity.ok(revenueByBrand);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid period value",
                    "validPeriods", Arrays.toString(TimePeriod.values()),
                    "provided", period
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch revenue by brand: " + e.getMessage()));
        }
    }

    @GetMapping("/test")
    @Operation(summary = "Test analytics endpoint")
    public ResponseEntity<Map<String, String>> testAnalytics() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Analytics API is working!",
                "timestamp", LocalDate.now().toString()
        ));
    }
}