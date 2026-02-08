package com.opticstore.product.glasses.model;

import com.opticstore.common.model.BaseEntity;
import com.opticstore.product.brand.model.Brand;
import com.opticstore.product.category.model.Category;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "glasses")
public class Glasses extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(
            mappedBy = "glasses",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("order ASC")
    private List<GlassesImage> images = new ArrayList<>();

    // CHANGE: Update category relationship to be nullable or remove if not needed
    // Keep category for other types (Sunglasses, Reading Glasses, etc.)
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category; // Now nullable since gender handles MEN/WOMEN

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private boolean active = true;

    // New fields for specifications
    @Column(name = "frame_material", length = 100)
    private String frameMaterial;

    @Column(name = "lens_material", length = 100)
    private String lensMaterial;

    @Column(name = "frame_color", length = 50)
    private String frameColor;

    @Column(name = "lens_color", length = 50)
    private String lensColor;

    // CHANGE: Make gender NOT NULL and add validation
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender = Gender.UNISEX;

    @Column(name = "frame_width", precision = 4, scale = 1)
    private BigDecimal frameWidth;

    @Column(name = "bridge_width", precision = 4, scale = 1)
    private BigDecimal bridgeWidth;

    @Column(name = "temple_length", precision = 4, scale = 1)
    private BigDecimal templeLength;

    @Column(name = "lens_width", precision = 4, scale = 1)
    private BigDecimal lensWidth;

    @Column(name = "lens_height", precision = 4, scale = 1)
    private BigDecimal lensHeight;

    // Enum for gender
    public enum Gender {
        MEN, WOMEN, UNISEX
    }

    // getters and setters...

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

    public List<GlassesImage> getImages() {
        return images;
    }

    public void setImages(List<GlassesImage> images) {
        this.images = images;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public BigDecimal getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(BigDecimal frameWidth) {
        this.frameWidth = frameWidth;
    }

    public BigDecimal getBridgeWidth() {
        return bridgeWidth;
    }

    public void setBridgeWidth(BigDecimal bridgeWidth) {
        this.bridgeWidth = bridgeWidth;
    }

    public BigDecimal getTempleLength() {
        return templeLength;
    }

    public void setTempleLength(BigDecimal templeLength) {
        this.templeLength = templeLength;
    }

    public BigDecimal getLensHeight() {
        return lensHeight;
    }

    public void setLensHeight(BigDecimal lensHeight) {
        this.lensHeight = lensHeight;
    }

    public BigDecimal getLensWidth() {
        return lensWidth;
    }

    public void setLensWidth(BigDecimal lensWidth) {
        this.lensWidth = lensWidth;
    }

    // Helper methods
    public boolean isInStock() {
        return this.active && this.quantity > 0;
    }

    public void decreaseQuantity(int amount) {
        if (amount > this.quantity) {
            throw new IllegalArgumentException("Not enough stock");
        }
        this.quantity -= amount;
    }
}