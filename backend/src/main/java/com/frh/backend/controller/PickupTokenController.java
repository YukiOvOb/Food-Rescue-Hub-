package com.frh.backend.controller;

import com.frh.backend.dto.PickupTokenResponseDto;
import com.frh.backend.model.Order;
import com.frh.backend.model.PickupToken;
import com.frh.backend.repository.OrderRepository;
import com.frh.backend.repository.PickupTokenRepository;
import com.frh.backend.util.QrCodeGenerator;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pickup-tokens")
public class PickupTokenController {

  private final PickupTokenRepository pickupTokenRepository;
  private final OrderRepository orderRepository;

  public PickupTokenController(
      PickupTokenRepository pickupTokenRepository, OrderRepository orderRepository) {
    this.pickupTokenRepository = pickupTokenRepository;
    this.orderRepository = orderRepository;
  }

  /** Retrieves the pickup token for an order, if present. */
  @GetMapping("/{orderId}")
  public ResponseEntity<?> getPickupToken(@PathVariable Long orderId) {
    try {
      return pickupTokenRepository
          .findByOrderId(orderId)
          .map(token -> ResponseEntity.ok(toResponse(token)))
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to fetch pickup token: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /** Generates a QR code image for the order's pickup token. */
  @PostMapping("/{orderId}/generate-qrcode")
  public ResponseEntity<Map<String, String>> generateQrCode(@PathVariable Long orderId) {
    PickupToken token =
        pickupTokenRepository
            .findByOrderId(orderId)
            .orElseGet(
                () -> {
                  // If token doesn't exist, create a new one
                  // First, find the Order entity
                  Order order =
                      orderRepository
                          .findById(orderId)
                          .orElseThrow(
                              () -> new RuntimeException("Order not found with id: " + orderId));

                  PickupToken newToken = new PickupToken();
                  newToken.setOrder(order); // Set the Order object, not just orderId
                  newToken.setQrTokenHash("QR-" + orderId + "-" + System.currentTimeMillis());
                  newToken.setIssuedAt(java.time.LocalDateTime.now());
                  newToken.setExpiresAt(java.time.LocalDateTime.now().plusDays(1));
                  return pickupTokenRepository.save(newToken);
                });

    try {
      String qrHash = token.getQrTokenHash();
      String safeHash = qrHash.replaceAll("[^A-Za-z0-9_-]", "_");
      String fileName = "order_" + orderId + "_" + safeHash;
      String filePath = QrCodeGenerator.generateQrCode(qrHash, fileName);
      String qrCodeUrl = "http://localhost:8080/qrcode/" + fileName + ".png";

      Map<String, String> response = new HashMap<>();
      response.put("orderId", String.valueOf(orderId));
      response.put("qrTokenHash", qrHash);
      response.put("qrCodePath", filePath);
      response.put("qrCodeUrl", qrCodeUrl);
      response.put("message", "QR code generated successfully");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to generate QR code: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  @PostMapping(value = "/decode-qrcode", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, String>> decodeQrCode(@RequestPart("file") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "No file uploaded");
      return ResponseEntity.badRequest().body(error);
    }

    try {
      BufferedImage image = ImageIO.read(file.getInputStream());
      if (image == null) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid image file");
        return ResponseEntity.badRequest().body(error);
      }

      BinaryBitmap bitmap =
          new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
      Result result = new MultiFormatReader().decode(bitmap);

      Map<String, String> response = new HashMap<>();
      response.put("content", result.getText());
      return ResponseEntity.ok(response);
    } catch (com.google.zxing.NotFoundException e) {
      // No QR code found in image - this is not an error, just no QR code detected
      Map<String, String> response = new HashMap<>();
      response.put("content", "");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to decode QR code: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  private PickupTokenResponseDto toResponse(PickupToken token) {
    PickupTokenResponseDto dto = new PickupTokenResponseDto();
    if (token == null) {
      return dto;
    }

    dto.setOrderId(token.getOrderId());
    dto.setQrTokenHash(token.getQrTokenHash());
    dto.setIssuedAt(token.getIssuedAt());
    dto.setExpiresAt(token.getExpiresAt());
    dto.setUsedAt(token.getUsedAt());
    return dto;
  }
}
