package com.opticstore.product.glasses.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;


public class GlassesCreateRequest {
    @NotBlank
    private String name;

    @NotNull @Positive
    private BigDecimal price;

    private String description;

    @NotNull
    @Size(min = 1, max = 4, message = "You must provide 1 to 4 images")
    private List<String> imageUrls;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long brandId;

    @NotNull @Min(0)
    private Integer quantity;

    private String frameMaterial;
    private String lensMaterial;
    private String frameColor;
    private String lensColor;
    private String gender;

    // Measurements as String
    private String frameWidth;
    private String bridgeWidth;
    private String templeLength;
    private String lensWidth;
    private String lensHeight;


    // getters and setters


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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getFrameMaterial() {
        return frameMaterial;
    }

    public void setFrameMaterial(String frameMaterial) {
        this.frameMaterial = frameMaterial;
    }

    public String getLensMaterial() {
        return lensMaterial;
    }

    public void setLensMaterial(String lensMaterial) {
        this.lensMaterial = lensMaterial;
    }

    public String getFrameColor() {
        return frameColor;
    }

    public void setFrameColor(String frameColor) {
        this.frameColor = frameColor;
    }

    public String getLensColor() {
        return lensColor;
    }

    public void setLensColor(String lensColor) {
        this.lensColor = lensColor;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(String frameWidth) {
        this.frameWidth = frameWidth;
    }

    public String getBridgeWidth() {
        return bridgeWidth;
    }

    public void setBridgeWidth(String bridgeWidth) {
        this.bridgeWidth = bridgeWidth;
    }

    public String getTempleLength() {
        return templeLength;
    }

    public void setTempleLength(String templeLength) {
        this.templeLength = templeLength;
    }

    public String getLensWidth() {
        return lensWidth;
    }

    public void setLensWidth(String lensWidth) {
        this.lensWidth = lensWidth;
    }

    public String getLensHeight() {
        return lensHeight;
    }

    public void setLensHeight(String lensHeight) {
        this.lensHeight = lensHeight;
    }

}