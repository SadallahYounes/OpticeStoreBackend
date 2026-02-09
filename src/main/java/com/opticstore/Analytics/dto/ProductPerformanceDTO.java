package com.opticstore.Analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPerformanceDTO {
    private ProductSummary topProduct;
    private List<ProductStats> topProducts;
    private InventoryMetrics inventoryMetrics;

    //  getters and setters
    public ProductSummary getTopProduct() { return topProduct; }
    public void setTopProduct(ProductSummary topProduct) { this.topProduct = topProduct; }

    public List<ProductStats> getTopProducts() { return topProducts; }
    public void setTopProducts(List<ProductStats> topProducts) { this.topProducts = topProducts; }

    public InventoryMetrics getInventoryMetrics() { return inventoryMetrics; }
    public void setInventoryMetrics(InventoryMetrics inventoryMetrics) { this.inventoryMetrics = inventoryMetrics; }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSummary {
        private Long id;
        private String name;
        private String brand;
        private String category;
        private BigDecimal totalRevenue;
        private Integer unitsSold;
        private String imageUrl;

        // Manual getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

        public Integer getUnitsSold() { return unitsSold; }
        public void setUnitsSold(Integer unitsSold) { this.unitsSold = unitsSold; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductStats {
        private Long productId;
        private String productName;
        private String brand;
        private String category;
        private Integer unitsSold;
        private BigDecimal revenue;
        private Integer stockQuantity;
        private BigDecimal stockValue;

        // Manual getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Integer getUnitsSold() { return unitsSold; }
        public void setUnitsSold(Integer unitsSold) { this.unitsSold = unitsSold; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

        public BigDecimal getStockValue() { return stockValue; }
        public void setStockValue(BigDecimal stockValue) { this.stockValue = stockValue; }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryMetrics {
        private Integer totalProducts;
        private Integer lowStockItems;
        private Integer outOfStockItems;
        private BigDecimal totalStockValue;

        // Manual getters and setters
        public Integer getTotalProducts() { return totalProducts; }
        public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }

        public Integer getLowStockItems() { return lowStockItems; }
        public void setLowStockItems(Integer lowStockItems) { this.lowStockItems = lowStockItems; }

        public Integer getOutOfStockItems() { return outOfStockItems; }
        public void setOutOfStockItems(Integer outOfStockItems) { this.outOfStockItems = outOfStockItems; }

        public BigDecimal getTotalStockValue() { return totalStockValue; }
        public void setTotalStockValue(BigDecimal totalStockValue) { this.totalStockValue = totalStockValue; }
    }
}