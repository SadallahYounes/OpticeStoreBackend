package com.opticstore.product.glasses.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;


public class GlassesCreateRequest {

    @NotBlank
    private String name;

    @NotNull @Positive
    private BigDecimal price;

    @NotNull @Size(min = 1, max = 4, message = "You must provide 1 to 4 images")
    private List<String> imageUrls;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long brandId;

    @NotNull @Min(0)
    private Integer quantity;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

