package com.frh.backend.controller;

import com.frh.backend.Model.Order;
import com.frh.backend.Model.PickupToken;
import com.frh.backend.repository.OrderRepository;
import com.frh.backend.repository.PickupTokenRepository;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.frh.backend.util.QRCodeGenerator.generateQRCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PickupTokenController.class)
class PickupTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PickupTokenRepository pickupTokenRepository;

    @MockBean
    private OrderRepository orderRepository;

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

        Mockito.when(pickupTokenRepository.findByOrderId(1L))
                .thenReturn(Optional.of(token));

        mockMvc.perform(get("/api/pickup-tokens/{orderId}", 1L))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       GET PICKUP TOKEN – NOT FOUND
       -------------------------------- */
    @Test
    void getPickupToken_notFound() throws Exception {

        Mockito.when(pickupTokenRepository.findByOrderId(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pickup-tokens/{orderId}", 99L))
                .andExpect(status().isNotFound());
    }

    /* --------------------------------
       GENERATE QR – TOKEN EXISTS
       -------------------------------- */
    @Test
    void generateQRCode_tokenExists() throws Exception {

        PickupToken token = new PickupToken();
        token.setQrTokenHash("QR-1");

        Mockito.when(pickupTokenRepository.findByOrderId(1L))
                .thenReturn(Optional.of(token));

        try (MockedStatic<com.frh.backend.util.QRCodeGenerator> mocked =
                     Mockito.mockStatic(com.frh.backend.util.QRCodeGenerator.class)) {

            mocked.when(() -> generateQRCode(Mockito.any(), Mockito.any()))
                    .thenReturn("/tmp/qrcode.png");

            mockMvc.perform(post("/api/pickup-tokens/{orderId}/generate-qrcode", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("QR code generated successfully"));
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

        Mockito.when(pickupTokenRepository.findByOrderId(2L))
                .thenReturn(Optional.empty());

        Mockito.when(orderRepository.findById(2L))
                .thenReturn(Optional.of(order));

        Mockito.when(pickupTokenRepository.save(Mockito.any()))
                .thenReturn(token);

        try (MockedStatic<com.frh.backend.util.QRCodeGenerator> mocked =
                     Mockito.mockStatic(com.frh.backend.util.QRCodeGenerator.class)) {

            mocked.when(() -> generateQRCode(Mockito.any(), Mockito.any()))
                    .thenReturn("/tmp/qrcode.png");

            mockMvc.perform(post("/api/pickup-tokens/{orderId}/generate-qrcode", 2L))
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

        Mockito.when(pickupTokenRepository.findByOrderId(1L))
                .thenReturn(Optional.of(token));

        try (MockedStatic<com.frh.backend.util.QRCodeGenerator> mocked =
                     Mockito.mockStatic(com.frh.backend.util.QRCodeGenerator.class)) {

            mocked.when(() -> generateQRCode(Mockito.any(), Mockito.any()))
                    .thenThrow(new RuntimeException("QR error"));

            mockMvc.perform(post("/api/pickup-tokens/{orderId}/generate-qrcode", 1L))
                    .andExpect(status().isInternalServerError());
        }
    }

    /* --------------------------------
       DECODE QR – NO FILE
       -------------------------------- */
    @Test
    void decodeQRCode_noFile() throws Exception {

        mockMvc.perform(multipart("/api/pickup-tokens/decode-qrcode"))
                .andExpect(status().isBadRequest());
    }

    /* --------------------------------
       DECODE QR – INVALID IMAGE
       -------------------------------- */
    @Test
    void decodeQRCode_invalidImage() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not-an-image".getBytes()
        );

        mockMvc.perform(multipart("/api/pickup-tokens/decode-qrcode")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid image file"));
    }
}
