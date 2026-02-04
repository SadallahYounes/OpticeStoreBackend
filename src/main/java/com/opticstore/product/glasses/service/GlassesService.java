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
        if (gender == null) return;

        try {
            glasses.setGender(Glasses.Gender.valueOf(gender.toUpperCase()));
        } catch (IllegalArgumentException e) {
            glasses.setGender(Glasses.Gender.UNISEX);
        }
    }

    // ===================== CUSTOMER METHODS =====================

    public List<GlassesResponse> getByCategory(String slug) {
        return glassesRepository.findByCategory_Slug(slug).stream()
                .filter(Glasses::isActive)
                .map(g -> GlassesResponse.fromEntity(g, imageUrlMapper))
                .toList();
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

    // ===================== ADMIN METHODS =====================

    public GlassesResponse create(GlassesCreateRequest request, Category category, Brand brand) {

        //  Price validation
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        // Quantity validation - Required and must be >= 0
        if (request.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        //  Images validation
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

        // Images - ONLY update if imageUrls are explicitly provided in the request
        // This fixes the issue where empty image array might be sent
        if (req.imageUrls() != null && !req.imageUrls().isEmpty()) {
            System.out.println("Image URLs provided in request. Count: " + req.imageUrls().size());

            // Validate image count
            if (req.imageUrls().size() > 4) {
                throw new IllegalArgumentException("Maximum 4 images allowed");
            }

            System.out.println("Current images in database: " + g.getImages().size());

            // Prepare new image URLs (remove base URL if present)
            List<String> newImageUrls = new ArrayList<>();
            for (String url : req.imageUrls()) {
                if (url == null || url.trim().isEmpty()) {
                    continue;
                }

                String processedUrl = url.trim();
                if (baseUrl != null && processedUrl.startsWith(baseUrl)) {
                    processedUrl = processedUrl.substring(baseUrl.length());
                }
                newImageUrls.add(processedUrl);
            }

            // Get current image URLs
            List<String> currentImageUrls = g.getImages().stream()
                    .sorted(Comparator.comparingInt(GlassesImage::getOrder))
                    .map(GlassesImage::getImageUrl)
                    .toList();

            System.out.println("Current image URLs: " + currentImageUrls);
            System.out.println("New image URLs: " + newImageUrls);

            // Check if images actually changed
            boolean imagesChanged = !currentImageUrls.equals(newImageUrls);

            if (imagesChanged) {
                System.out.println("Images have changed, updating...");

                // Clear existing images from database first
                if (!g.getImages().isEmpty()) {
                    glassesImageRepository.deleteAll(g.getImages());
                    g.getImages().clear();
                }

                // Add new images
                for (int i = 0; i < newImageUrls.size(); i++) {
                    GlassesImage image = new GlassesImage();
                    image.setImageUrl(newImageUrls.get(i));
                    image.setOrder(i);
                    image.setGlasses(g);
                    g.getImages().add(image);
                }

                System.out.println("Added " + g.getImages().size() + " new images");
            } else {
                System.out.println("Images unchanged, skipping image update");
            }
        } else if (req.imageUrls() != null && req.imageUrls().isEmpty()) {
            // If empty array is sent, validate for active products
            if (g.isActive() && (req.active() == null || req.active())) {
                throw new IllegalArgumentException("At least one image is required for active products");
            }
            System.out.println("Empty image array provided, keeping existing images");
        } else {
            // imageUrls is null - don't touch existing images
            System.out.println("No image URLs provided in request, keeping existing images");
        }

        try {
            // Save and return response
            Glasses saved = glassesRepository.save(g);
            System.out.println("=== UPDATE SUCCESSFUL ===");
            System.out.println("Successfully updated glasses ID: " + saved.getId());
            System.out.println("Final image count: " + saved.getImages().size());

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
}
