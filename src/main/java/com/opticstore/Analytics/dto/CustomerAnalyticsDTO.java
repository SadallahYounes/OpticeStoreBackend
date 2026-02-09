package com.opticstore.Analytics.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalyticsDTO {
    private Long uniqueCustomers;
    private Long periodOrders;
    private BigDecimal avgOrdersPerCustomer;
    private List<WilayaData> topWilayas;

    // Manual getters/setters if Lombok not working
    public Long getUniqueCustomers() { return uniqueCustomers; }
    public void setUniqueCustomers(Long uniqueCustomers) { this.uniqueCustomers = uniqueCustomers; }

    public Long getPeriodOrders() { return periodOrders; }
    public void setPeriodOrders(Long periodOrders) { this.periodOrders = periodOrders; }

    public BigDecimal getAvgOrdersPerCustomer() { return avgOrdersPerCustomer; }
    public void setAvgOrdersPerCustomer(BigDecimal avgOrdersPerCustomer) { this.avgOrdersPerCustomer = avgOrdersPerCustomer; }

    public List<WilayaData> getTopWilayas() { return topWilayas; }
    public void setTopWilayas(List<WilayaData> topWilayas) { this.topWilayas = topWilayas; }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WilayaData {
        private String wilaya;
        private Long orders;

        // Manual getters/setters
        public String getWilaya() { return wilaya; }
        public void setWilaya(String wilaya) { this.wilaya = wilaya; }

        public Long getOrders() { return orders; }
        public void setOrders(Long orders) { this.orders = orders; }
    }
}
