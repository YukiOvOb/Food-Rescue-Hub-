package com.frh.backend.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class QrCodeResourceConfig implements WebMvcConfigurer {

  /** Serves locally generated QR code images under the /qrcode/** path. */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path qrDir = Paths.get("QRCode").toAbsolutePath();
    String qrDirUri = qrDir.toUri().toString();

    registry.addResourceHandler("/qrcode/**").addResourceLocations(qrDirUri);
  }
}
