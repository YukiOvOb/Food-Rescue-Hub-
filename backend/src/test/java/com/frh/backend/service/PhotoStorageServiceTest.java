package com.frh.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhotoStorageServiceTest {

    private static final String LOCAL_PREFIX = "/uploads/listings/";

    private final PhotoStorageService service = new PhotoStorageService();
    private Path createdFile;

    @AfterEach
    void cleanupUploadedFile() throws IOException {
        if (createdFile != null) {
            Files.deleteIfExists(createdFile);
        }
    }

    @Test
    void store_whenR2Disabled_savesLocallyAndReturnsRelativeUrl() throws IOException {
        ReflectionTestUtils.setField(service, "r2Enabled", false);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                "image-bytes".getBytes());

        String url = service.store(42L, file);

        assertTrue(url.startsWith(LOCAL_PREFIX + "listing_42_"));
        assertTrue(url.endsWith(".jpg"));

        String filename = url.substring(LOCAL_PREFIX.length());
        createdFile = Paths.get("uploads", "listings", filename).toAbsolutePath();
        assertTrue(Files.exists(createdFile));
    }

    @Test
    void store_whenR2EnabledAndEndpointInvalid_throwsRuntimeException() {
        setR2Fields(
                true,
                "listing-photos",
                "not-a-valid-uri",
                "https://cdn.example.com",
                "access",
                "secret");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.png",
                "image/png",
                "image-bytes".getBytes());

        assertThrows(RuntimeException.class, () -> service.store(7L, file));
    }

    @Test
    void useR2_returnsFalseWhenDisabled() {
        setR2Fields(
                false,
                "listing-photos",
                "https://example.r2.cloudflarestorage.com",
                "https://cdn.example.com",
                "access",
                "secret");

        assertFalse(invokeUseR2());
    }

    @Test
    void useR2_returnsFalseWhenBucketMissing() {
        setR2Fields(
                true,
                "   ",
                "https://example.r2.cloudflarestorage.com",
                "https://cdn.example.com",
                "access",
                "secret");

        assertFalse(invokeUseR2());
    }

    @Test
    void useR2_returnsFalseWhenEndpointMissing() {
        setR2Fields(
                true,
                "listing-photos",
                " ",
                "https://cdn.example.com",
                "access",
                "secret");

        assertFalse(invokeUseR2());
    }

    @Test
    void useR2_returnsFalseWhenPublicBaseMissing() {
        setR2Fields(
                true,
                "listing-photos",
                "https://example.r2.cloudflarestorage.com",
                "",
                "access",
                "secret");

        assertFalse(invokeUseR2());
    }

    @Test
    void useR2_returnsFalseWhenAccessKeyMissing() {
        setR2Fields(
                true,
                "listing-photos",
                "https://example.r2.cloudflarestorage.com",
                "https://cdn.example.com",
                null,
                "secret");

        assertFalse(invokeUseR2());
    }

    @Test
    void useR2_returnsFalseWhenSecretKeyMissing() {
        setR2Fields(
                true,
                "listing-photos",
                "https://example.r2.cloudflarestorage.com",
                "https://cdn.example.com",
                "access",
                " ");

        assertFalse(invokeUseR2());
    }

    @Test
    void useR2_returnsTrueWhenAllConfigPresent() {
        setR2Fields(
                true,
                "listing-photos",
                "https://example.r2.cloudflarestorage.com",
                "https://cdn.example.com",
                "access",
                "secret");

        assertTrue(invokeUseR2());
    }

    @Test
    void getExtension_returnsEmptyWhenNameIsNull() {
        assertEquals("", invokeGetExtension(null));
    }

    @Test
    void getExtension_returnsEmptyWhenNoDotPresent() {
        assertEquals("", invokeGetExtension("filename"));
    }

    @Test
    void getExtension_returnsSuffixWhenDotPresent() {
        assertEquals(".jpeg", invokeGetExtension("photo.jpeg"));
    }

    @Test
    void isBlank_handlesNullWhitespaceAndText() {
        assertTrue(invokeIsBlank(null));
        assertTrue(invokeIsBlank("   "));
        assertFalse(invokeIsBlank("value"));
    }

    private void setR2Fields(
            boolean enabled,
            String bucket,
            String endpoint,
            String publicBase,
            String accessKey,
            String secretKey) {
        ReflectionTestUtils.setField(service, "r2Enabled", enabled);
        ReflectionTestUtils.setField(service, "bucket", bucket);
        ReflectionTestUtils.setField(service, "endpoint", endpoint);
        ReflectionTestUtils.setField(service, "publicBase", publicBase);
        ReflectionTestUtils.setField(service, "accessKey", accessKey);
        ReflectionTestUtils.setField(service, "secretKey", secretKey);
    }

    private boolean invokeUseR2() {
        return ReflectionTestUtils.invokeMethod(service, "useR2");
    }

    private String invokeGetExtension(String originalName) {
        return ReflectionTestUtils.invokeMethod(PhotoStorageService.class, "getExtension", originalName);
    }

    private boolean invokeIsBlank(String value) {
        return ReflectionTestUtils.invokeMethod(PhotoStorageService.class, "isBlank", value);
    }
}
