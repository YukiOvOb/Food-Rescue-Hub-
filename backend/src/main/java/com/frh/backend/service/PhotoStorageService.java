package com.frh.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Handles photo storage. Uses Cloudflare R2 (S3-compatible) when enabled; falls back to local disk
 * otherwise.
 */
@Service
public class PhotoStorageService {

  @Value("${storage.r2.enabled:false}")
  private boolean r2Enabled;

  @Value("${storage.r2.bucket:}")
  private String bucket;

  @Value("${storage.r2.endpoint:}")
  private String endpoint;

  @Value("${storage.r2.public-base:}")
  private String publicBase;

  @Value("${storage.r2.access-key:}")
  private String accessKey;

  @Value("${storage.r2.secret-key:}")
  private String secretKey;

  private static final Path LOCAL_DIR = Paths.get("uploads", "listings").toAbsolutePath();

  /** Stores a listing photo and returns a URL that clients can load. */
  public String store(Long listingId, MultipartFile file) throws IOException {
    String ext = getExtension(file.getOriginalFilename());
    String filename = "listing_" + listingId + "_" + System.currentTimeMillis() + ext;

    if (useR2()) {
      return uploadToR2(file.getInputStream(), file.getSize(), filename, file.getContentType());
    }

    return saveToLocal(file.getInputStream(), filename);
  }

  private boolean useR2() {
    return r2Enabled
        && !isBlank(bucket)
        && !isBlank(endpoint)
        && !isBlank(publicBase)
        && !isBlank(accessKey)
        && !isBlank(secretKey);
  }

  private String uploadToR2(InputStream input, long contentLength, String key, String contentType)
      throws IOException {
    AwsBasicCredentials creds = AwsBasicCredentials.create(accessKey, secretKey);
    try (S3Client s3 =
        S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(creds))
            .region(Region.US_EAST_1) // R2 accepts any region label
            .forcePathStyle(true)
            .build()) {

      PutObjectRequest req =
          PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build();

      s3.putObject(
          req, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(input, contentLength));
    }

    String base =
        publicBase.endsWith("/") ? publicBase.substring(0, publicBase.length() - 1) : publicBase;
    return base + "/" + key;
  }

  private String saveToLocal(InputStream input, String filename) throws IOException {
    Files.createDirectories(LOCAL_DIR);
    Path target = LOCAL_DIR.resolve(filename);
    Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
    return "/uploads/listings/" + filename;
  }

  private static String getExtension(String originalName) {
    if (originalName == null) return "";
    int dot = originalName.lastIndexOf('.');
    return dot >= 0 ? originalName.substring(dot) : "";
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
