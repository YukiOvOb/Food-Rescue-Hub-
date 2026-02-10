package com.frh.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Exposes uploaded listing photos under /uploads/** so the mobile app
 * can load them with an absolute URL.
 */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads").toAbsolutePath();
        String uploadUri = uploadDir.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadUri);
    }
}
