package com.frh.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class QRCodeResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path qrDir = Paths.get("QRCode").toAbsolutePath();
        String qrDirUri = qrDir.toUri().toString();

        registry.addResourceHandler("/qrcode/**")
                .addResourceLocations(qrDirUri);
    }
}
