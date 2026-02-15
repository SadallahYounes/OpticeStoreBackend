package com.opticstore.customer.controller;

import com.opticstore.customer.dto.CustomerDTO;
import com.opticstore.customer.dto.CustomerDetailsDTO;
import com.opticstore.customer.dto.CustomerStatsDTO;
import com.opticstore.customer.service.CustomerService;
import com.opticstore.order.repository.OrderRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/customers")
@Tag(name = "Customer Management", description = "Customer management endpoints for admin")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerController {

    private final CustomerService customerService;
    private final OrderRepository orderRepository;

    public CustomerController(CustomerService customerService,OrderRepository orderRepository) {
        this.customerService = customerService;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> getCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String wilaya,
            @RequestParam(required = false) String segment,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "totalSpent") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<CustomerDTO> customers = customerService.getCustomers(search, wilaya, segment, page, size, sortBy, sortDir);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/stats")
    public ResponseEntity<CustomerStatsDTO> getCustomerStats() {
        CustomerStatsDTO stats = customerService.getCustomerStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDetailsDTO> getCustomerDetails(@PathVariable Long id) {
        CustomerDetailsDTO customer = customerService.getCustomerDetails(id);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDTO>> searchCustomers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CustomerDTO> customers = customerService.searchCustomers(q, page, size);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/segments")
    public ResponseEntity<Map<String, Object>> getCustomerSegments() {
        Map<String, Object> segments = customerService.getCustomerSegments();
        return ResponseEntity.ok(segments);
    }

    @GetMapping("/top")
    public ResponseEntity<List<CustomerDTO>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        List<CustomerDTO> customers = customerService.getTopCustomers(limit);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/repeat-rate")
    public ResponseEntity<Map<String, Object>> getRepeatCustomerRate() {
        Map<String, Object> rate = customerService.getRepeatCustomerRate();
        return ResponseEntity.ok(rate);
    }

    @GetMapping("/acquisition")
    public ResponseEntity<List<Map<String, Object>>> getCustomerAcquisition(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Map<String, Object>> acquisition = customerService.getCustomerAcquisition(startDate, endDate);
        return ResponseEntity.ok(acquisition);
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<Map<String, Object>>> getCustomerOrders(@PathVariable Long id) {
        List<Map<String, Object>> orders = customerService.getCustomerOrders(id);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}/insights")
    public ResponseEntity<Map<String, Object>> getCustomerInsights(@PathVariable Long id) {
        Map<String, Object> insights = customerService.getCustomerInsights(id);
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/wilayas")
    public ResponseEntity<List<String>> getWilayas() {
        List<String> wilayas = customerService.getWilayas();
        return ResponseEntity.ok(wilayas);
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<Void> addCustomerNote(
            @PathVariable Long id,
            @RequestBody Map<String, String> note) {
        customerService.addCustomerNote(id, note.get("note"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public byte[] exportCustomers() {
        Pageable pageable = PageRequest.of(0, 10000);
        List<Object[]> data = orderRepository.findAllCustomers(pageable);

        StringBuilder csv = new StringBuilder();
        csv.append("Name,Phone,Wilaya,Baladia,Total Orders,Total Spent,Last Order Date,First Order Date\n");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Object[] row : data) {
            csv.append(String.format("\"%s\",%s,%s,%s,%d,%.2f,%s,%s\n",
                    row[1] != null ? row[1].toString().replace("\"", "\"\"") : "",
                    row[0] != null ? row[0].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    row[3] != null ? row[3].toString() : "",
                    ((Number) row[4]).intValue(),
                    ((BigDecimal) row[5]).doubleValue(),
                    row[6] != null ? ((LocalDateTime) row[6]).format(dateFormatter) : "",
                    row[7] != null ? ((LocalDateTime) row[7]).format(dateFormatter) : ""
            ));
        }

        return csv.toString().getBytes();
    }
}