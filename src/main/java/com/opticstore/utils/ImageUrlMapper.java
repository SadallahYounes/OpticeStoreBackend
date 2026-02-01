package com.opticstore.utils;

import com.opticstore.config.AppConfig;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlMapper {

    private final String baseUrl;

    // Constructor injection
    public ImageUrlMapper(AppConfig appConfig) {
        this.baseUrl = appConfig.getBaseUrl();
    }

    public String toFullUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        // If it's already a full URL, return as is
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }

        // If it starts with /uploads, prepend base URL
        if (imagePath.startsWith("/uploads")) {
            return baseUrl + imagePath;
        }

        // If it's just a filename, prepend base URL and uploads path
        return baseUrl + "/uploads/" + imagePath;
    }
}