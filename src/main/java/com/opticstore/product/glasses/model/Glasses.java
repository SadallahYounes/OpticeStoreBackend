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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "glasses_id")
    @OrderColumn(name = "image_order")
    private List<GlassesImage> images = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private boolean active = true;

    // Helper methods for images
    public void addImage(String imageUrl) {
        GlassesImage image = new GlassesImage();
        image.setImageUrl(imageUrl);
        image.setGlasses(this);
        this.images.add(image);
    }

    public void removeImage(GlassesImage image) {
        this.images.remove(image);
        image.setGlasses(null);
    }

    // getters & setters
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
}