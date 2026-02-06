package com.frh.backend.controller;

import com.frh.backend.dto.CartResponseDto;
import com.frh.backend.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    /* -------------------------------
       GET CART
       ------------------------------- */
    @Test
    void getCart_success() throws Exception {

        CartResponseDto cart = Mockito.mock(CartResponseDto.class);

        Mockito.when(cartService.getOrCreateActiveCart(Mockito.any(HttpSession.class)))
                .thenReturn(cart);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk());
    }

    /* -------------------------------
       ADD ITEM
       ------------------------------- */
    @Test
    void addItem_success() throws Exception {

        Map<String, Object> request = new HashMap<>();
        request.put("listingId", 10L);
        request.put("qty", 2);

        CartResponseDto updatedCart = Mockito.mock(CartResponseDto.class);

        Mockito.when(cartService.addItem(
                Mockito.any(HttpSession.class),
                Mockito.eq(10L),
                Mockito.eq(2)))
                .thenReturn(updatedCart);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /* -------------------------------
       UPDATE QUANTITY
       ------------------------------- */
    @Test
    void updateQuantity_success() throws Exception {

        Map<String, Object> request = new HashMap<>();
        request.put("qty", 5);

        CartResponseDto updatedCart = Mockito.mock(CartResponseDto.class);

        Mockito.when(cartService.updateQuantity(
                Mockito.any(HttpSession.class),
                Mockito.eq(20L),
                Mockito.eq(5)))
                .thenReturn(updatedCart);

        mockMvc.perform(patch("/api/cart/items/{listingId}", 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /* -------------------------------
       REMOVE ITEM
       ------------------------------- */
    @Test
    void removeItem_success() throws Exception {

        CartResponseDto cart = Mockito.mock(CartResponseDto.class);

        Mockito.when(cartService.removeItem(
                Mockito.any(HttpSession.class),
                Mockito.eq(30L)))
                .thenReturn(cart);

        mockMvc.perform(delete("/api/cart/items/{listingId}", 30L))
                .andExpect(status().isOk());
    }

    /* -------------------------------
       CLEAR CART
       ------------------------------- */
    @Test
    void clearCart_success() throws Exception {

        CartResponseDto cart = Mockito.mock(CartResponseDto.class);

        Mockito.when(cartService.clearCart(Mockito.any(HttpSession.class)))
                .thenReturn(cart);

        mockMvc.perform(delete("/api/cart/items"))
                .andExpect(status().isOk());
    }
}
