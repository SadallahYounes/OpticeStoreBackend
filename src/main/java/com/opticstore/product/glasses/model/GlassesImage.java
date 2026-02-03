package com.opticstore.product.glasses.model;

import com.opticstore.common.model.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "glasses_images")
public class GlassesImage extends BaseEntity {

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "image_order")
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "glasses_id", nullable = false)
    private Glasses glasses;

    // getters & setters
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Glasses getGlasses() {
        return glasses;
    }

    public void setGlasses(Glasses glasses) {
        this.glasses = glasses;
    }
}