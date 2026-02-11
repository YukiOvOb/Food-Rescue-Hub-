package com.frh.backend.controller;

import static com.frh.backend.util.QrCodeGenerator.generateQrCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.frh.backend.model.Order;
import com.frh.backend.model.PickupToken;
import com.frh.backend.repository.OrderRepository;
import com.frh.backend.repository.PickupTokenRepository;
import com.frh.backend.service.OrderService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PickupTokenController.class)
class PickupTokenControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PickupTokenRepository pickupTokenRepository;

  @MockitoBean private OrderRepository orderRepository;

  @MockitoBean private OrderService orderService;

  /* --------------------------------
  GET PICKUP TOKEN – FOUND
  -------------------------------- */
  @Test
  void getPickupToken_found() throws Exception {

    PickupToken token = new PickupToken();
    Order order = new Order();
    order.setOrderId(1L);
    token.setOrderId(1L);
    token.setOrder(order);
    token.setQrTokenHash("QR-1");
    token.setExpiresAt(LocalDateTime.now().plusDays(1));

    Mockito.when(pickupTokenRepository.findByOrderId(1L)).thenReturn(Optional.of(token));

    mockMvc.perform(get("/api/pickup-tokens/{orderId}", 1L)).andExpect(status().isOk());
  }

  /* --------------------------------
  GET PICKUP TOKEN – NOT FOUND
  -------------------------------- */
  @Test
  void getPickupToken_notFound() throws Exception {

    Mockito.when(pickupTokenRepository.findByOrderId(99L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/pickup-tokens/{orderId}", 99L)).andExpect(status().isNotFound());
  }

  /* --------------------------------
  GENERATE QR – TOKEN EXISTS
  -------------------------------- */
  @Test
  void generateQRCode_tokenExists() throws Exception {

    PickupToken token = new PickupToken();
    token.setQrTokenHash("QR-1");

    Mockito.when(pickupTokenRepository.findByOrderId(1L)).thenReturn(Optional.of(token));

    try (MockedStatic<com.frh.backend.util.QrCodeGenerator> mocked =
        Mockito.mockStatic(com.frh.backend.util.QrCodeGenerator.class)) {

      mocked.when(() -> generateQrCode(Mockito.any(), Mockito.any())).thenReturn("/tmp/qrcode.png");

      mockMvc
          .perform(post("/api/pickup-tokens/{orderId}/generate-qrcode", 1L))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("QR code generated successfully"));
    }
  }

  /* --------------------------------
  GENERATE QR – TOKEN CREATED
  -------------------------------- */
  @Test
  void generateQRCode_createNewToken() throws Exception {

    Order order = new Order();
    order.setOrderId(2L);

    PickupToken token = new PickupToken();
    token.setQrTokenHash("QR-2");

    Mockito.when(pickupTokenRepository.findByOrderId(2L)).thenReturn(Optional.empty());

    Mockito.when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

    Mockito.when(pickupTokenRepository.save(Mockito.any())).thenReturn(token);

    try (MockedStatic<com.frh.backend.util.QrCodeGenerator> mocked =
        Mockito.mockStatic(com.frh.backend.util.QrCodeGenerator.class)) {

      mocked.when(() -> generateQrCode(Mockito.any(), Mockito.any())).thenReturn("/tmp/qrcode.png");

      mockMvc
          .perform(post("/api/pickup-tokens/{orderId}/generate-qrcode", 2L))
          .andExpect(status().isOk());
    }
  }

  /* --------------------------------
  GENERATE QR – ERROR
  -------------------------------- */
  @Test
  void generateQRCode_error() throws Exception {

    PickupToken token = new PickupToken();
    token.setQrTokenHash("QR-ERR");

    Mockito.when(pickupTokenRepository.findByOrderId(1L)).thenReturn(Optional.of(token));

    try (MockedStatic<com.frh.backend.util.QrCodeGenerator> mocked =
        Mockito.mockStatic(com.frh.backend.util.QrCodeGenerator.class)) {

      mocked
          .when(() -> generateQrCode(Mockito.any(), Mockito.any()))
          .thenThrow(new RuntimeException("QR error"));

      mockMvc
          .perform(post("/api/pickup-tokens/{orderId}/generate-qrcode", 1L))
          .andExpect(status().isInternalServerError());
    }
  }

  /* --------------------------------
  DECODE QR – NO FILE
  -------------------------------- */
  @Test
  void decodeQRCode_noFile() throws Exception {

    mockMvc
        .perform(multipart("/api/pickup-tokens/decode-qrcode"))
        .andExpect(status().isBadRequest());
  }

  /* --------------------------------
  DECODE QR – INVALID IMAGE
  -------------------------------- */
  @Test
  void decodeQRCode_invalidImage() throws Exception {

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "not-an-image".getBytes());

    mockMvc
        .perform(multipart("/api/pickup-tokens/decode-qrcode").file(file))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Invalid image file"));
  }

  /* --------------------------------
  GET PICKUP TOKEN – ERROR
  -------------------------------- */
  @Test
  void getPickupToken_error() throws Exception {

    Mockito.when(pickupTokenRepository.findByOrderId(1L))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/pickup-tokens/{orderId}", 1L))
        .andExpect(status().isInternalServerError());
  }

  /* --------------------------------
  GENERATE QR – ORDER NOT FOUND
  -------------------------------- */
  @Test
  void generateQRCode_orderNotFound() throws Exception {

    Mockito.when(pickupTokenRepository.findByOrderId(1L)).thenReturn(Optional.empty());

    Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    mockMvc
        .perform(post("/api/pickup-tokens/{orderId}/generate-qrcode", 1L))
        .andExpect(status().isNotFound());
  }

  /* --------------------------------
  DECODE QR – READ FAILURE
  -------------------------------- */
  @Test
  void decodeQRCode_readFailure() throws Exception {

    MockMultipartFile invalidImage =
        new MockMultipartFile(
            "file",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            new byte[] {-1, -40, -1} // Invalid JPEG header
            );

    mockMvc
        .perform(multipart("/api/pickup-tokens/decode-qrcode").file(invalidImage))
        .andExpect(status().isInternalServerError());
  }

  /* --------------------------------
  GENERATE QR – SAVE FAILURE
  -------------------------------- */
  @Test
  void generateQRCode_saveFailure() throws Exception {

    Order order = new Order();
    order.setOrderId(3L);

    Mockito.when(pickupTokenRepository.findByOrderId(3L)).thenReturn(Optional.empty());

    Mockito.when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

    Mockito.when(pickupTokenRepository.save(Mockito.any()))
        .thenThrow(new RuntimeException("Save failed"));

    try (MockedStatic<com.frh.backend.util.QrCodeGenerator> mocked =
        Mockito.mockStatic(com.frh.backend.util.QrCodeGenerator.class)) {

      mocked
          .when(() -> generateQrCode(Mockito.any(), Mockito.any()))
          .thenThrow(new RuntimeException("QR generation failed"));

      mockMvc
          .perform(post("/api/pickup-tokens/{orderId}/generate-qrcode", 3L))
          .andExpect(status().isInternalServerError());
    }
  }

  /* --------------------------------
  GET PICKUP TOKEN BY ORDER ID – ZERO ID
  -------------------------------- */
  @Test
  void getPickupToken_zeroOrderId() throws Exception {

    Mockito.when(pickupTokenRepository.findByOrderId(0L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/pickup-tokens/{orderId}", 0L)).andExpect(status().isNotFound());
  }

  /* --------------------------------
  GENERATE QR – EXISTING TOKEN PROPERTIES
  -------------------------------- */
  @Test
  void generateQRCode_tokenExistsPropertyCheck() throws Exception {

    PickupToken token = new PickupToken();
    token.setQrTokenHash("QR-EXISTING");
    token.setOrderId(4L);
    token.setExpiresAt(LocalDateTime.now().plusDays(2));

    Mockito.when(pickupTokenRepository.findByOrderId(4L)).thenReturn(Optional.of(token));

    try (MockedStatic<com.frh.backend.util.QrCodeGenerator> mocked =
        Mockito.mockStatic(com.frh.backend.util.QrCodeGenerator.class)) {

      mocked
          .when(() -> generateQrCode(Mockito.eq("QR-EXISTING"), Mockito.any()))
          .thenReturn("/qrcode/order_4_QR-EXISTING.png");

      mockMvc
          .perform(post("/api/pickup-tokens/{orderId}/generate-qrcode", 4L))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.qrTokenHash").value("QR-EXISTING"));
    }
  }

  /* --------------------------------
  GENERATE QR – FILE ENCODING
  -------------------------------- */
  @Test
  void generateQRCode_filePathEncoding() throws Exception {

    PickupToken token = new PickupToken();
    token.setQrTokenHash("QR-SPECIAL-CHARS-123");

    Mockito.when(pickupTokenRepository.findByOrderId(5L)).thenReturn(Optional.of(token));

    try (MockedStatic<com.frh.backend.util.QrCodeGenerator> mocked =
        Mockito.mockStatic(com.frh.backend.util.QrCodeGenerator.class)) {

      mocked
          .when(() -> generateQrCode(Mockito.any(), Mockito.any()))
          .thenReturn("/tmp/qrcode_special.png");

      mockMvc
          .perform(post("/api/pickup-tokens/{orderId}/generate-qrcode", 5L))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.qrCodePath").value("/tmp/qrcode_special.png"));
    }
  }

  /* --------------------------------
  DECODE QR – EMPTY FILE
  -------------------------------- */
  @Test
  void decodeQRCode_emptyFile() throws Exception {

    MockMultipartFile emptyFile =
        new MockMultipartFile(
            "file", "empty.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {} // Empty file
            );

    mockMvc
        .perform(multipart("/api/pickup-tokens/decode-qrcode").file(emptyFile))
        .andExpect(status().isBadRequest());
  }

  /* --------------------------------
  VERIFY TOKEN – MISSING HASH
  -------------------------------- */
  @Test
  void verifyToken_missingHash() throws Exception {

    mockMvc
        .perform(
            post("/api/pickup-tokens/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("QR token hash is required"));
  }

  /* --------------------------------
  VERIFY TOKEN – EMPTY HASH
  -------------------------------- */
  @Test
  void verifyToken_emptyHash() throws Exception {

    mockMvc
        .perform(
            post("/api/pickup-tokens/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"qrTokenHash\": \"   \"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("QR token hash is required"));
  }

  /* --------------------------------
  VERIFY TOKEN – TOKEN NOT FOUND
  -------------------------------- */
  @Test
  void verifyToken_tokenNotFound() throws Exception {

    Mockito.when(pickupTokenRepository.findByQrTokenHash("INVALID"))
        .thenReturn(java.util.Optional.empty());

    mockMvc
        .perform(
            post("/api/pickup-tokens/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"qrTokenHash\": \"INVALID\"}"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("Failed to verify token: Invalid pickup token"));
  }

  /* --------------------------------
  VERIFY TOKEN – TOKEN EXPIRED
  -------------------------------- */
  @Test
  void verifyToken_tokenExpired() throws Exception {

    PickupToken token = new PickupToken();
    token.setQrTokenHash("EXPIRED-TOKEN");
    token.setExpiresAt(LocalDateTime.now().minusDays(1));

    Mockito.when(pickupTokenRepository.findByQrTokenHash("EXPIRED-TOKEN"))
        .thenReturn(java.util.Optional.of(token));

    mockMvc
        .perform(
            post("/api/pickup-tokens/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"qrTokenHash\": \"EXPIRED-TOKEN\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Pickup token has expired"));
  }

  /* --------------------------------
  VERIFY TOKEN – TOKEN ALREADY USED
  -------------------------------- */
  @Test
  void verifyToken_tokenAlreadyUsed() throws Exception {

    PickupToken token = new PickupToken();
    token.setQrTokenHash("USED-TOKEN");
    token.setExpiresAt(LocalDateTime.now().plusDays(1));
    token.setUsedAt(LocalDateTime.now().minusHours(1));

    Mockito.when(pickupTokenRepository.findByQrTokenHash("USED-TOKEN"))
        .thenReturn(java.util.Optional.of(token));

    mockMvc
        .perform(
            post("/api/pickup-tokens/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"qrTokenHash\": \"USED-TOKEN\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Pickup token has already been used"));
  }

  /* --------------------------------
  VERIFY TOKEN – SUCCESS
  -------------------------------- */
  @Test
  void verifyToken_success() throws Exception {

    PickupToken token = new PickupToken();
    token.setQrTokenHash("VALID-TOKEN");
    token.setOrderId(100L);
    token.setExpiresAt(LocalDateTime.now().plusDays(1));
    token.setUsedAt(null);

    Order completedOrder = new Order();
    completedOrder.setOrderId(100L);
    completedOrder.setStatus("COMPLETED");

    Mockito.when(pickupTokenRepository.findByQrTokenHash("VALID-TOKEN"))
        .thenReturn(java.util.Optional.of(token));

    Mockito.when(orderService.completeOrder(100L)).thenReturn(completedOrder);

    mockMvc
        .perform(
            post("/api/pickup-tokens/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"qrTokenHash\": \"VALID-TOKEN\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Order completed successfully"))
        .andExpect(jsonPath("$.orderId").value(100L))
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  /* --------------------------------
  VERIFY TOKEN – SERVICE ERROR
  -------------------------------- */
  @Test
  void verifyToken_serviceError() throws Exception {

    PickupToken token = new PickupToken();
    token.setQrTokenHash("ERROR-TOKEN");
    token.setOrderId(200L);
    token.setExpiresAt(LocalDateTime.now().plusDays(1));
    token.setUsedAt(null);

    Mockito.when(pickupTokenRepository.findByQrTokenHash("ERROR-TOKEN"))
        .thenReturn(java.util.Optional.of(token));

    Mockito.when(orderService.completeOrder(200L))
        .thenThrow(new RuntimeException("Order completion failed"));

    mockMvc
        .perform(
            post("/api/pickup-tokens/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"qrTokenHash\": \"ERROR-TOKEN\"}"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("Failed to verify token: Order completion failed"));
  }
}

