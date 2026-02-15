package com.opticstore.customer.service;

import com.opticstore.customer.dto.CustomerDTO;
import com.opticstore.customer.dto.CustomerDetailsDTO;
import com.opticstore.customer.dto.CustomerStatsDTO;
import com.opticstore.customer.dto.OrderSummaryDTO;
import com.opticstore.order.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final OrderRepository orderRepository;

    public CustomerService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Page<CustomerDTO> getCustomers(String search, String wilaya, String segment,
                                          int page, int size, String sortBy, String sortDir) {

        Pageable pageable = PageRequest.of(page, size);
        List<Object[]> customerData;
        Long totalCount;

        if (search != null && !search.isEmpty()) {
            // Use search with pagination
            customerData = orderRepository.searchCustomers(search, pageable);
            totalCount = orderRepository.countSearchCustomers(search);
        } else {
            // Get all customers with pagination
            customerData = orderRepository.findAllCustomers(pageable);
            totalCount = orderRepository.countTotalCustomers();
        }

        // Convert to DTOs
        List<CustomerDTO> customers = customerData.stream()
                .map(this::mapToCustomerDTO)
                .collect(Collectors.toList());

        // Apply filters (wilaya and segment) in memory
        List<CustomerDTO> filteredCustomers = customers.stream()
                .filter(c -> wilaya == null || wilaya.equals("all") || wilaya.equals(c.getWilaya()))
                .filter(c -> {
                    if (segment == null || segment.equals("all")) return true;
                    String customerSegment = determineSegment(c.getTotalOrders(), c.getTotalSpent());
                    return customerSegment.equalsIgnoreCase(segment);
                })
                .collect(Collectors.toList());

        // Since we're filtering in memory, we need to adjust pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredCustomers.size());

        // If start is beyond the list size, return empty list
        if (start >= filteredCustomers.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, filteredCustomers.size());
        }

        List<CustomerDTO> paginatedCustomers = filteredCustomers.subList(start, end);

        return new PageImpl<>(paginatedCustomers, pageable, filteredCustomers.size());
    }

    public CustomerStatsDTO getCustomerStats() {
        CustomerStatsDTO stats = new CustomerStatsDTO();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        Long totalCustomers = orderRepository.countTotalCustomers();
        Long activeCustomers = orderRepository.countActiveCustomers(thirtyDaysAgo);
        Long newCustomersThisMonth = orderRepository.countNewCustomers(startOfMonth);
        Long repeatCustomers = orderRepository.countRepeatCustomers();
        Long oneTimeCustomers = orderRepository.countOneTimeCustomers();
        Long vipCustomers = orderRepository.countVIPCustomers();

        BigDecimal totalRevenue = orderRepository.totalRevenue(); // Already filtered for DELIVERED

        stats.setTotalCustomers(totalCustomers != null ? totalCustomers : 0L);
        stats.setActiveCustomers(activeCustomers != null ? activeCustomers : 0L);
        stats.setNewCustomersThisMonth(newCustomersThisMonth != null ? newCustomersThisMonth : 0L);
        stats.setRepeatCustomers(repeatCustomers != null ? repeatCustomers : 0L);
        stats.setOneTimeCustomers(oneTimeCustomers != null ? oneTimeCustomers : 0L);
        stats.setVipCustomers(vipCustomers != null ? vipCustomers : 0L);

        // Calculate percentages
        if (totalCustomers != null && totalCustomers > 0) {
            // Calculate as double with one decimal place
            double activePercentage = (activeCustomers * 100.0) / totalCustomers;
            double repeatRate = (repeatCustomers * 100.0) / totalCustomers;

            // Round to one decimal place
            stats.setActivePercentage(Math.round(activePercentage * 10.0) / 10.0);
            stats.setRepeatRate(Math.round(repeatRate * 10.0) / 10.0);
        }

        // Calculate average lifetime value
        if (totalCustomers != null && totalCustomers > 0 && totalRevenue != null) {
            BigDecimal avgLTV = totalRevenue.divide(
                    BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP);
            stats.setAverageLifetimeValue(avgLTV);
        }

        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // Get customers by wilaya
        List<Object[]> wilayaData = orderRepository.countCustomersByWilaya();
        Map<String, Long> customersByWilaya = new LinkedHashMap<>();
        for (Object[] data : wilayaData) {
            customersByWilaya.put(data[0].toString(), ((Number) data[1]).longValue());
        }
        stats.setCustomersByWilaya(customersByWilaya);

        // Get customers by segment
        List<Object[]> segmentData = orderRepository.countCustomersBySegment();
        Map<String, Long> customersBySegment = new LinkedHashMap<>();
        for (Object[] data : segmentData) {
            customersBySegment.put(data[0].toString(), ((Number) data[1]).longValue());
        }
        stats.setCustomersBySegment(customersBySegment);

        return stats;
    }

    public CustomerDetailsDTO getCustomerDetails(Long id) {
        // Note: This assumes you have a way to get phone by customer ID
        // You might need a Customer entity or a mapping table
        String phone = getPhoneByCustomerId(id); // Implement this method

        List<Object[]> customerData = orderRepository.findCustomerByPhone(phone);
        if (customerData.isEmpty()) {
            throw new RuntimeException("Customer not found");
        }

        CustomerDetailsDTO details = new CustomerDetailsDTO();
        Object[] data = customerData.get(0);

        details.setPhone((String) data[0]);
        details.setName((String) data[1]);
        details.setWilaya((String) data[2]);
        details.setBaladia((String) data[3]);
        details.setTotalOrders(((Number) data[4]).intValue());
        details.setTotalSpent((BigDecimal) data[5]);
        details.setLastOrderDate((LocalDateTime) data[6]);
        details.setCreatedAt((LocalDateTime) data[7]);
        details.setAverageOrderValue((BigDecimal) data[8]);

        // Determine segment
        details.setCustomerSegment(determineSegment(
                details.getTotalOrders(),
                details.getTotalSpent()
        ));

        // Get recent orders
        List<Object[]> ordersData = orderRepository.findOrdersByCustomerPhone(phone);
        List<OrderSummaryDTO> recentOrders = ordersData.stream()
                .map(o -> {
                    OrderSummaryDTO order = new OrderSummaryDTO();
                    order.setOrderId(((Number) o[0]).longValue());
                    order.setTotal((BigDecimal) o[1]);
                    order.setStatus((String) o[2]);
                    order.setCreatedAt((LocalDateTime) o[3]);
                    order.setItemCount(((Number) o[4]).intValue());
                    return order;
                })
                .limit(5)
                .collect(Collectors.toList());
        details.setRecentOrders(recentOrders);

        // Get top products
        List<Object[]> productsData = orderRepository.findCustomerTopProducts(phone);
        List<Map<String, Object>> topProducts = productsData.stream()
                .map(p -> {
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", p[0]);
                    product.put("name", p[1]);
                    product.put("brand", p[2]);
                    product.put("category", p[3]);
                    product.put("quantity", ((Number) p[4]).intValue());
                    product.put("spent", p[5]);
                    return product;
                })
                .collect(Collectors.toList());
        details.setTopProducts(topProducts);

        // Calculate days since last order
        if (details.getLastOrderDate() != null) {
            long days = ChronoUnit.DAYS.between(
                    details.getLastOrderDate().toLocalDate(),
                    LocalDate.now()
            );
            details.setDaysSinceLastOrder((int) days);
        }

        return details;
    }

    public Page<CustomerDTO> searchCustomers(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<Object[]> customerData = orderRepository.searchCustomers(query, pageable);

        // You need a count query for search results
        // Add this method to OrderRepository first
        Long totalCount = orderRepository.countSearchCustomers(query);

        List<CustomerDTO> customers = customerData.stream()
                .map(this::mapToCustomerDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(customers, pageable, totalCount != null ? totalCount : 0L);
    }

    public Map<String, Object> getCustomerSegments() {
        Map<String, Object> segments = new LinkedHashMap<>();

        List<Object[]> segmentData = orderRepository.countCustomersBySegment();
        for (Object[] data : segmentData) {
            segments.put((String) data[0], ((Number) data[1]).longValue());
        }

        return segments;
    }

    public List<CustomerDTO> getTopCustomers(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("total_spent").descending());
        List<Object[]> data = orderRepository.findAllCustomers(pageable);
        return data.stream()
                .map(this::mapToCustomerDTO)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getRepeatCustomerRate() {
        Map<String, Object> rate = new HashMap<>();

        Long totalCustomers = orderRepository.countTotalCustomers();
        Long repeatCustomers = orderRepository.countRepeatCustomers();

        rate.put("totalCustomers", totalCustomers);
        rate.put("repeatCustomers", repeatCustomers);
        rate.put("oneTimeCustomers", totalCustomers - repeatCustomers);

        if (totalCustomers != null && totalCustomers > 0) {
            double repeatRate = (repeatCustomers * 100.0) / totalCustomers;
            rate.put("repeatRate", Math.round(repeatRate * 10.0) / 10.0);
        }

        return rate;
    }

    public List<Map<String, Object>> getCustomerAcquisition(LocalDate startDate, LocalDate endDate) {
        // This would require a new query in OrderRepository
        // For now, return empty list
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getCustomerOrders(Long customerId) {
        String phone = getPhoneByCustomerId(customerId);
        List<Object[]> ordersData = orderRepository.findOrdersByCustomerPhone(phone);

        return ordersData.stream()
                .map(o -> {
                    Map<String, Object> order = new HashMap<>();
                    order.put("id", o[0]);
                    order.put("total", o[1]);
                    order.put("status", o[2]);
                    order.put("createdAt", o[3]);
                    order.put("itemCount", o[4]);
                    return order;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getCustomerInsights(Long customerId) {
        String phone = getPhoneByCustomerId(customerId);
        Map<String, Object> insights = new HashMap<>();

        // Get favorite category
        // This would require a new query
        insights.put("favoriteCategory", "Sunglasses");
        insights.put("favoriteBrand", "Ray-Ban");
        insights.put("averageDaysBetweenOrders", 15);
        insights.put("preferredWilaya", "Algiers");

        return insights;
    }

    public List<String> getWilayas() {
        List<Object[]> data = orderRepository.countCustomersByWilaya();
        return data.stream()
                .map(d -> d[0].toString())
                .collect(Collectors.toList());
    }

    public void addCustomerNote(Long customerId, String note) {
        // Implement if you have a notes table
    }

    public byte[] exportCustomers() {
        Pageable pageable = PageRequest.of(0, 10000); // Get all customers
        List<Object[]> data = orderRepository.findAllCustomers(pageable);

        StringBuilder csv = new StringBuilder();
        csv.append("Name,Phone,Email,Wilaya,Baladia,Total Orders,Total Spent,Last Order Date\n");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Object[] row : data) {
            csv.append(String.format("\"%s\",%s,%s,%s,%s,%d,%.2f,%s\n",
                    row[1] != null ? row[1].toString() : "",
                    row[0] != null ? row[0].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    row[3] != null ? row[3].toString() : "",
                    row[4] != null ? row[4].toString() : "",
                    ((Number) row[5]).intValue(),
                    ((BigDecimal) row[6]).doubleValue(),
                    row[7] != null ? ((LocalDateTime) row[7]).format(dateFormatter) : ""
            ));
        }

        return csv.toString().getBytes();
    }

    // Helper methods
    private CustomerDTO mapToCustomerDTO(Object[] data) {
        CustomerDTO dto = new CustomerDTO();

        // Handle phone - could be String or Long
        Object phoneObj = data[0];
        if (phoneObj != null) {
            dto.setPhone(phoneObj.toString()); // Convert to String safely
        } else {
            dto.setPhone(null);
        }

        // Handle name
        dto.setName(data[1] != null ? data[1].toString() : null);

        // Handle wilaya
        dto.setWilaya(data[2] != null ? data[2].toString() : null);

        // Handle baladia
        dto.setBaladia(data[3] != null ? data[3].toString() : null);

        // Handle totalOrders (should be Number)
        if (data[4] != null) {
            dto.setTotalOrders(((Number) data[4]).intValue());
        } else {
            dto.setTotalOrders(0);
        }

        // Handle totalSpent (should be BigDecimal)
        if (data[5] != null) {
            if (data[5] instanceof BigDecimal) {
                dto.setTotalSpent((BigDecimal) data[5]);
            } else {
                dto.setTotalSpent(new BigDecimal(data[5].toString()));
            }
        } else {
            dto.setTotalSpent(BigDecimal.ZERO);
        }

        // Handle lastOrderDate
        if (data[6] != null) {
            if (data[6] instanceof LocalDateTime) {
                dto.setLastOrderDate((LocalDateTime) data[6]);
            } else if (data[6] instanceof java.sql.Timestamp) {
                dto.setLastOrderDate(((java.sql.Timestamp) data[6]).toLocalDateTime());
            }
        }

        // Handle createdAt (firstOrderDate)
        if (data.length > 7 && data[7] != null) {
            if (data[7] instanceof LocalDateTime) {
                dto.setCreatedAt((LocalDateTime) data[7]);
            } else if (data[7] instanceof java.sql.Timestamp) {
                dto.setCreatedAt(((java.sql.Timestamp) data[7]).toLocalDateTime());
            }
        }

        // Generate an ID from phone (since we don't have a real ID)
        if (dto.getPhone() != null) {
            dto.setId((long) dto.getPhone().hashCode());
        } else {
            dto.setId(System.currentTimeMillis()); // Fallback
        }

        return dto;
    }
    private String determineSegment(int totalOrders, BigDecimal totalSpent) {
        if (totalOrders >= 10 || (totalSpent != null && totalSpent.compareTo(new BigDecimal("100000")) >= 0)) {
            return "VIP";
        } else if (totalOrders >= 3) {
            return "Regular";
        } else if (totalOrders >= 1) {
            return "New";
        } else {
            return "Inactive";
        }
    }

    private String getPhoneByCustomerId(Long id) {
        // This is a placeholder - you need to implement this based on your data model
        // You might need a Customer entity or a mapping table
        return "0555555555"; // Replace with actual implementation
    }
}