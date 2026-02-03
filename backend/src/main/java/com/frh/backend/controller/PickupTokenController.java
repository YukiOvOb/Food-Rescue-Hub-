package com.frh.backend.controller;

import com.frh.backend.Model.PickupToken;
import com.frh.backend.repository.PickupTokenRepository;
import com.frh.backend.util.QRCodeGenerator;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pickup-tokens")
public class PickupTokenController {

    private final PickupTokenRepository pickupTokenRepository;

    public PickupTokenController(PickupTokenRepository pickupTokenRepository) {
        this.pickupTokenRepository = pickupTokenRepository;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PickupToken> getPickupToken(@PathVariable Long orderId) {
        return pickupTokenRepository.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
// http://172.26.235.205:8080/api/pickup-tokens/1/generate-qrcode
    @PostMapping("/{orderId}/generate-qrcode")
    public ResponseEntity<Map<String, String>> generateQRCode(@PathVariable Long orderId) {
        return pickupTokenRepository.findByOrderId(orderId)
                .map(token -> {
                    try {
                        String qrHash = token.getQrTokenHash();
                        String fileName = "order_" + orderId + "_" + qrHash;
                        String filePath = QRCodeGenerator.generateQRCode(qrHash, fileName);
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
                        return ResponseEntity.status(500).body(error);
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/decode-qrcode")
    public ResponseEntity<Map<String, String>> decodeQRCode(@RequestParam("file") MultipartFile file) {
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

            BinaryBitmap bitmap = new BinaryBitmap(
                    new HybridBinarizer(new BufferedImageLuminanceSource(image))
            );
            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

            Result result = new MultiFormatReader().decode(bitmap, hints);

            Map<String, String> response = new HashMap<>();
            response.put("content", result.getText());
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No QR code found in the image");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to decode QR code: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
