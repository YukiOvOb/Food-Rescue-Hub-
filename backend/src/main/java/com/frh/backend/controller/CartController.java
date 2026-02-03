package com.frh.backend.controller;

import com.frh.backend.dto.CartResponseDto;
import com.frh.backend.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDto> getCart(HttpSession session) {
        CartResponseDto cart = cartService.getOrCreateActiveCart(session);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItem(
            HttpSession session,
            @RequestBody Map<String, Object> request) {

        Long listingId = Long.valueOf(request.get("listingId").toString());
        Integer qty = Integer.valueOf(request.get("qty").toString());

        CartResponseDto updatedCart = cartService.addItem(session, listingId, qty);
        return ResponseEntity.ok(updatedCart);
    }

    @PatchMapping("/items/{listingId}")
    public ResponseEntity<CartResponseDto> updateQuantity(
            HttpSession session,
            @PathVariable Long listingId,
            @RequestBody Map<String, Object> request) {

        Integer qty = Integer.valueOf(request.get("qty").toString());
        CartResponseDto updatedCart = cartService.updateQuantity(session, listingId, qty);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/items/{listingId}")
    public ResponseEntity<CartResponseDto> removeItem(
            HttpSession session,
            @PathVariable Long listingId) {

        CartResponseDto cart = cartService.removeItem(session, listingId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items")
    public ResponseEntity<CartResponseDto> clearCart(HttpSession session) {
        CartResponseDto cart = cartService.clearCart(session);
        return ResponseEntity.ok(cart);
    }
}