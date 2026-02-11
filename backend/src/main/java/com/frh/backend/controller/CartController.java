package com.frh.backend.controller;

import com.frh.backend.dto.CartResponseDto;
import com.frh.backend.service.CartService;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
      HttpSession session, @RequestBody Map<String, Object> request) {

    Object listingIdRaw = request.get("listingId");
    Object qtyRaw = request.get("qty");
    if (listingIdRaw == null || qtyRaw == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "listingId and qty are required");
    }

    Long listingId;
    Integer qty;
    try {
      listingId = Long.valueOf(listingIdRaw.toString());
      qty = Integer.valueOf(qtyRaw.toString());
    } catch (NumberFormatException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "listingId and qty must be numeric");
    }

    CartResponseDto updatedCart = cartService.addItem(session, listingId, qty);
    return ResponseEntity.ok(updatedCart);
  }

  @PatchMapping("/items/{listingId}")
  public ResponseEntity<CartResponseDto> updateQuantity(
      HttpSession session, @PathVariable Long listingId, @RequestBody Map<String, Object> request) {

    Object qtyRaw = request.get("qty");
    if (qtyRaw == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "qty is required");
    }

    Integer qty;
    try {
      qty = Integer.valueOf(qtyRaw.toString());
    } catch (NumberFormatException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "qty must be numeric");
    }

    CartResponseDto updatedCart = cartService.updateQuantity(session, listingId, qty);
    return ResponseEntity.ok(updatedCart);
  }

  @DeleteMapping("/items/{listingId}")
  public ResponseEntity<CartResponseDto> removeItem(
      HttpSession session, @PathVariable Long listingId) {

    CartResponseDto cart = cartService.removeItem(session, listingId);
    return ResponseEntity.ok(cart);
  }

  @DeleteMapping("/items")
  public ResponseEntity<CartResponseDto> clearCart(HttpSession session) {
    CartResponseDto cart = cartService.clearCart(session);
    return ResponseEntity.ok(cart);
  }
}
