package com.frh.backend.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/** Utility for generating QR code images backed by ZXing. */
public class QrCodeGenerator {

  private static final String QR_CODE_DIR = "QRCode";
  private static final int WIDTH = 300;
  private static final int HEIGHT = 300;

  /**
   * Generate QR code and save to local directory.
   *
   * @param text QR code content (e.g., qrTokenHash)
   * @param fileName File name (without extension)
   * @return Saved file path
   */
  public static String generateQrCode(String text, String fileName)
      throws WriterException, IOException {
    Path directory = Paths.get(QR_CODE_DIR);
    if (!Files.exists(directory)) {
      Files.createDirectories(directory);
    }

    Map<EncodeHintType, Object> hints = new HashMap<>();
    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
    hints.put(EncodeHintType.MARGIN, 1);

    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);

    Path filePath = directory.resolve(fileName + ".png");
    MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

    return filePath.toAbsolutePath().toString();
  }
}
