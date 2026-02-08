package com.opticstore.product.glasses.service;

import com.opticstore.product.brand.model.Brand;
import com.opticstore.product.brand.repository.BrandRepository;
import com.opticstore.product.category.model.Category;
import com.opticstore.product.category.repository.CategoryRepository;
import com.opticstore.product.glasses.dto.GlassesAdminResponse;
import com.opticstore.product.glasses.dto.GlassesCreateRequest;
import com.opticstore.product.glasses.dto.GlassesResponse;
import com.opticstore.product.glasses.dto.GlassesUpdateRequest;
import com.opticstore.product.glasses.model.Glasses;
import com.opticstore.product.glasses.model.GlassesImage;
import com.opticstore.product.glasses.repository.GlassesImageRepository;
import com.opticstore.product.glasses.repository.GlassesRepository;
import com.opticstore.utils.ImageUrlMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GlassesService {

    private final GlassesRepository glassesRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final GlassesImageRepository glassesImageRepository;
    private final ImageUrlMapper imageUrlMapper;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public GlassesService(
            GlassesRepository glassesRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            GlassesImageRepository glassesImageRepository,
            ImageUrlMapper imageUrlMapper
    ) {
        this.glassesRepository = glassesRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.glassesImageRepository = glassesImageRepository;
        this.imageUrlMapper = imageUrlMapper;
    }

    // ===================== HELPERS =====================

    private String getFullImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }
        return baseUrl + imagePath;
    }

    private BigDecimal parseDecimal(String value, String fieldName) {
        System.out.println("Parsing " + fieldName + " value: '" + value + "'");

        if (value == null || value.trim().isEmpty()) {
            System.out.println(fieldName + " is null or empty, returning null");
            return null;
        }

        String trimmed = value.trim();

        // Handle special cases
        if (trimmed.equalsIgnoreCase("null") ||
                trimmed.equalsIgnoreCase("undefined") ||
                trimmed.equalsIgnoreCase("")) {
            System.out.println(fieldName + " is special value '" + trimmed + "', returning null");
            return null;
        }

        try {
            BigDecimal result = new BigDecimal(trimmed);
            System.out.println("Successfully parsed " + fieldName + ": " + result);
            return result;
        } catch (NumberFormatException e) {
            System.out.println("WARNING: Invalid " + fieldName + " format: '" + value + "'. Setting to null.");
            return null;
        }
    }

    private void applyGender(Glasses glasses, String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be null or empty");
        }

        try {
            glasses.setGender(Glasses.Gender.valueOf(gender.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid gender: " + gender + ". Must be MEN, WOMEN, or UNISEX");
        }
    }

    private String normalizeUrl(String url) {
        if (url == null) return "";
        url = url.trim();

        if (baseUrl != null && url.startsWith(baseUrl)) {
            url = url.substring(baseUrl.length());
        }

        if (!url.startsWith("/") && !url.startsWith("http")) {
            url = "/" + url;
        }

        return url;
    }

    // ===================== CUSTOMER METHODS =====================


    public List<GlassesResponse> getByGender(String genderStr) {
        try {
            Glasses.Gender gender = Glasses.Gender.valueOf(genderStr.toUpperCase());
            return glassesRepository.findByGenderAndActiveTrue(gender).stream()
                    .map(g -> GlassesResponse.fromEntity(g, imageUrlMapper))
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid gender: " + genderStr);
        }
    }

    public List<GlassesResponse> getByBrand(String brand) {
        return glassesRepository.findByBrand_NameIgnoreCase(brand).stream()
                .filter(Glasses::isActive)
                .map(g -> GlassesResponse.fromEntity(g, imageUrlMapper))
                .toList();
    }

    public List<GlassesResponse> getAllForCustomers() {
        return glassesRepository.findByActiveTrue().stream()
                .map(g -> GlassesResponse.fromEntity(g, imageUrlMapper))
                .toList();
    }

    public GlassesResponse getById(Long id) {
        Glasses glasses = glassesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Glasses not found"));

        if (!glasses.isActive()) {
            throw new RuntimeException("Product is not available");
        }

        return GlassesResponse.fromEntity(glasses, imageUrlMapper);
    }

    public List<GlassesResponse> getByBrandId(Long brandId) {
        return glassesRepository.findByBrandIdAndActiveTrue(brandId).stream()
                .map(g -> GlassesResponse.fromEntity(g, imageUrlMapper))
                .toList();
    }


    public List<GlassesResponse> getByGenderAndBrand(String genderStr, String brand) {
        try {
            Glasses.Gender gender = Glasses.Gender.valueOf(genderStr.toUpperCase());
            return glassesRepository.findByGenderAndBrandNameIgnoreCaseAndActiveTrue(gender, brand).stream()
                    .map(g -> GlassesResponse.fromEntity(g, imageUrlMapper))
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid gender: " + genderStr);
        }
    }

    public List<GlassesResponse> getAllActive() {
        return glassesRepository.findByActiveTrue().stream()
                .map(g -> GlassesResponse.fromEntity(g, imageUrlMapper))
                .toList();
    }


    // ===================== ADMIN METHODS =====================

    public GlassesResponse create(GlassesCreateRequest request, Category category, Brand brand) {
        // Price validation
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        // Quantity validation - Required and must be >= 0
        if (request.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        // Images validation
        if (request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            throw new IllegalArgumentException("At least one image is required");
        }

        if (request.getImageUrls().size() > 4) {
            throw new IllegalArgumentException("Maximum 4 images allowed");
        }

        Glasses glasses = new Glasses();
        glasses.setName(request.getName());
        glasses.setPrice(request.getPrice());
        glasses.setDescription(request.getDescription());
        glasses.setQuantity(request.getQuantity());
        glasses.setCategory(category);
        glasses.setBrand(brand);
        glasses.setActive(true);

        // Specifications
        glasses.setFrameMaterial(request.getFrameMaterial());
        glasses.setLensMaterial(request.getLensMaterial());
        glasses.setFrameColor(request.getFrameColor());
        glasses.setLensColor(request.getLensColor());
        applyGender(glasses, request.getGender());

        glasses.setFrameWidth(parseDecimal(request.getFrameWidth(), "frame width"));
        glasses.setBridgeWidth(parseDecimal(request.getBridgeWidth(), "bridge width"));
        glasses.setTempleLength(parseDecimal(request.getTempleLength(), "temple length"));
        glasses.setLensWidth(parseDecimal(request.getLensWidth(), "lens width"));
        glasses.setLensHeight(parseDecimal(request.getLensHeight(), "lens height"));


        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("Gender is required");
        }
        applyGender(glasses, request.getGender());

        // Images
        for (int i = 0; i < request.getImageUrls().size(); i++) {
            GlassesImage image = new GlassesImage();
            image.setImageUrl(request.getImageUrls().get(i));
            image.setOrder(i);
            image.setGlasses(glasses);
            glasses.getImages().add(image);
        }

        Glasses saved = glassesRepository.save(glasses);
        return GlassesResponse.fromEntity(saved, imageUrlMapper);
    }

    public List<GlassesAdminResponse> getAllForAdmin() {
        return glassesRepository.findAll().stream()
                .map(g -> GlassesAdminResponse.fromEntity(g, baseUrl))
                .toList();
    }



    public GlassesAdminResponse updateGlass(Long id, GlassesUpdateRequest req) {
        System.out.println("=== UPDATE GLASSES REQUEST ===");
        System.out.println("ID: " + id);
        System.out.println("Request: " + req);

        Glasses g = glassesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Glasses not found with ID: " + id));

        // ===========================
        // BASIC VALIDATIONS & FIELDS
        // ===========================

        // Price validation
        if (req.price() != null && req.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        // Basic fields
        if (req.name() != null) g.setName(req.name());
        if (req.price() != null) g.setPrice(req.price());
        if (req.description() != null) g.setDescription(req.description());

        // Quantity validation
        if (req.quantity() != null) {
            if (req.quantity() < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
            g.setQuantity(req.quantity());
        }

        if (req.active() != null) g.setActive(req.active());

        // Specifications
        if (req.frameMaterial() != null) g.setFrameMaterial(req.frameMaterial());
        if (req.lensMaterial() != null) g.setLensMaterial(req.lensMaterial());
        if (req.frameColor() != null) g.setFrameColor(req.frameColor());
        if (req.lensColor() != null) g.setLensColor(req.lensColor());

        // Gender
        if (req.gender() != null) {
            applyGender(g, req.gender());
        }

        // Measurements
        if (req.frameWidth() != null) g.setFrameWidth(parseDecimal(req.frameWidth(), "frame width"));
        if (req.bridgeWidth() != null) g.setBridgeWidth(parseDecimal(req.bridgeWidth(), "bridge width"));
        if (req.templeLength() != null) g.setTempleLength(parseDecimal(req.templeLength(), "temple length"));
        if (req.lensWidth() != null) g.setLensWidth(parseDecimal(req.lensWidth(), "lens width"));
        if (req.lensHeight() != null) g.setLensHeight(parseDecimal(req.lensHeight(), "lens height"));

        // Category update
        if (req.categoryId() != null) {
            Category category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + req.categoryId()));
            g.setCategory(category);
        }

        // Brand update
        if (req.brandId() != null) {
            Brand brand = brandRepository.findById(req.brandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found with ID: " + req.brandId()));
            g.setBrand(brand);
        }

        // ===========================
        // IMAGES - SMART UPDATE (KEEP / ADD)
        // ===========================
        System.out.println("Keep Image URLs: " + req.keepImageUrls());
        System.out.println("New Image URLs: " + req.newImageUrls());

        int totalImages = req.totalImageCount();
        if (totalImages > 4) {
            throw new IllegalArgumentException("Maximum 4 images allowed. You have: " + totalImages);
        }

        boolean willBeActive = req.active() != null ? req.active() : g.isActive();
        if (totalImages == 0 && willBeActive) {
            throw new IllegalArgumentException("At least one image is required for active products");
        }

        System.out.println("Current images in database: " + g.getImages().size());

        // ---- KEEP images ----
        if (req.keepImageUrls() != null) {
            List<GlassesImage> imagesToRemove = new ArrayList<>();

            for (GlassesImage existingImage : g.getImages()) {
                String existingUrl = normalizeUrl(existingImage.getImageUrl());

                boolean shouldKeep = req.keepImageUrls().stream()
                        .map(this::normalizeUrl)
                        .anyMatch(existingUrl::equals);

                if (!shouldKeep) {
                    imagesToRemove.add(existingImage);
                }
            }

            if (!imagesToRemove.isEmpty()) {
                System.out.println("Removing " + imagesToRemove.size() + " images...");
                g.getImages().removeAll(imagesToRemove);
                glassesImageRepository.deleteAll(imagesToRemove);
            }
        } else {
            // Remove all if keep list is null
            if (!g.getImages().isEmpty()) {
                System.out.println("Removing all existing images...");
                glassesImageRepository.deleteAll(g.getImages());
                g.getImages().clear();
            }
        }

        // ---- ADD new images ----
        if (req.newImageUrls() != null && !req.newImageUrls().isEmpty()) {
            int currentMaxOrder = g.getImages().stream()
                    .mapToInt(GlassesImage::getOrder)
                    .max()
                    .orElse(-1);

            for (int i = 0; i < req.newImageUrls().size(); i++) {
                String url = req.newImageUrls().get(i);
                if (url == null || url.trim().isEmpty()) continue;

                String processedUrl = url.trim();
                if (baseUrl != null && processedUrl.startsWith(baseUrl)) {
                    processedUrl = processedUrl.substring(baseUrl.length());
                }

                GlassesImage image = new GlassesImage();
                image.setImageUrl(processedUrl);
                image.setOrder(currentMaxOrder + i + 1);
                image.setGlasses(g);
                g.getImages().add(image);
            }
        }

        // ---- REORDER images ----
        List<GlassesImage> orderedImages = g.getImages().stream()
                .sorted(Comparator.comparingInt(GlassesImage::getOrder))
                .collect(Collectors.toList());

        for (int i = 0; i < orderedImages.size(); i++) {
            orderedImages.get(i).setOrder(i);
        }

        System.out.println("Final image count: " + g.getImages().size());

        try {
            Glasses saved = glassesRepository.save(g);
            System.out.println("=== UPDATE SUCCESSFUL ===");
            System.out.println("Successfully updated glasses ID: " + saved.getId());

            return GlassesAdminResponse.fromEntity(saved, baseUrl);
        } catch (Exception e) {
            System.out.println("=== UPDATE FAILED ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void updateStock(Long glassesId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Glasses glasses = glassesRepository.findById(glassesId)
                .orElseThrow(() -> new RuntimeException("Glasses not found"));

        glasses.setQuantity(newQuantity);
    }

    public void delete(Long id) {
        Glasses g = glassesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Glasses not found"));

        if (!g.isActive()) {
            throw new RuntimeException("Product is already deleted");
        }

        g.setActive(false);
        glassesRepository.save(g);
    }

    public List<GlassesResponse> getByBrandIdAndGender(Long brandId, String genderStr) {
        try {
            Glasses.Gender gender = Glasses.Gender.valueOf(genderStr.toUpperCase());
            return glassesRepository.findByBrandIdAndGenderAndActiveTrue(brandId, gender)
                    .stream()
                    .map(g -> GlassesResponse.fromEntity(g, imageUrlMapper))
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid gender: " + genderStr);
        }
    }
}
