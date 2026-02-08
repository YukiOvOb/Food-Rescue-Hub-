package com.frh.backend.util;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QRCodeGeneratorTest {

    private final List<Path> createdFiles = new ArrayList<>();

    @AfterEach
    void tearDown() throws IOException {
        for (Path file : createdFiles) {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void generateQRCode_createsPngAndReturnsAbsolutePath() throws WriterException, IOException {
        String fileName = "qr-test-" + UUID.randomUUID();

        String outputPath = QRCodeGenerator.generateQRCode("hello-world", fileName);
        Path path = Path.of(outputPath);
        createdFiles.add(path);

        assertTrue(path.isAbsolute());
        assertTrue(Files.exists(path));
        assertEquals(".png", extension(path));
    }

    @Test
    void generateQRCode_worksWhenDirectoryAlreadyExists() throws WriterException, IOException {
        String fileName1 = "qr-test-" + UUID.randomUUID();
        String fileName2 = "qr-test-" + UUID.randomUUID();

        Path first = Path.of(QRCodeGenerator.generateQRCode("first", fileName1));
        Path second = Path.of(QRCodeGenerator.generateQRCode("second", fileName2));
        createdFiles.add(first);
        createdFiles.add(second);

        assertTrue(Files.exists(first));
        assertTrue(Files.exists(second));
    }

    private static String extension(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.substring(fileName.lastIndexOf('.'));
    }
}
