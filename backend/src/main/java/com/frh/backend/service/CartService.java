package com.frh.backend.service;

import com.frh.backend.dto.CartResponseDto;
import com.frh.backend.exception.CrossStoreException;
import com.frh.backend.model.Cart;
import com.frh.backend.model.CartItem;
import com.frh.backend.model.ConsumerProfile;
import com.frh.backend.model.Listing;
import com.frh.backend.repository.CartItemRepository;
import com.frh.backend.repository.CartRepository;
import com.frh.backend.repository.ConsumerProfileRepository;
import com.frh.backend.repository.ListingRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ListingRepository listingRepository;
  private final ConsumerProfileRepository consumerProfileRepository;

  /**
   * Retrieves the active cart for the current session user. If no active cart exists, a new one is
   * created and returned as a CartResponseDto.
   *
   * @param session the HTTP session containing the user ID
   * @return the active {@link CartResponseDto} associated with the current user
   * @throws ResponseStatusException if the user is not authenticated or if the consumer does not
   *     exist
   */
  public CartResponseDto getOrCreateActiveCart(HttpSession session) {
    Cart cart = getOrCreateActiveCartEntity(session);
    return toCartResponseDto(cart);
  }

  /** Returns the list of CartItem entities for a given cart ID. */
  public List<CartItem> getCartItems(Long cartId) {
    return cartItemRepository.findByCart_CartId(cartId);
  }

  /**
   * Adds an item to the user's cart and returns the updated cart as a CartResponseDto.
   *
   * @param session the HTTP session containing the user ID
   * @param listingId the ID of the listing to add
   * @param qty the quantity to add
   * @return the updated {@link CartResponseDto}
   * @throws ResponseStatusException if quantity is invalid or listing not found
   * @throws CrossStoreException if trying to add items from a different store
   */
  @Transactional
  public CartResponseDto addItem(HttpSession session, Long listingId, int qty) {
    if (qty <= 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
    }

    Listing listing =
        listingRepository
            .findById(listingId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

    Cart cart = getOrCreateActiveCartEntity(session);

    if (cart.getStore() != null
        && !cart.getStore().getStoreId().equals(listing.getStore().getStoreId())) {
      throw new CrossStoreException(
          "Cannot add items from multiple stores to the same cart", cart.getStore().getStoreId());
    }

    cart.setStore(listing.getStore());
    cartRepository.save(cart);

    Optional<CartItem> existingItem =
        cartItemRepository.findByCart_CartIdAndListing_ListingId(cart.getCartId(), listingId);

    if (existingItem.isPresent()) {
      CartItem item = existingItem.get();
      item.setQuantity(item.getQuantity() + qty);
      cartItemRepository.save(item);
    } else {
      CartItem newItem = new CartItem();
      newItem.setCart(cart);
      newItem.setListing(listing);
      newItem.setQuantity(qty);
      cartItemRepository.save(newItem);
    }

    return toCartResponseDto(cart);
  }

  /**
   * Updates the quantity of an item in the cart and returns the updated cart as a CartResponseDto.
   * If the quantity is zero or less, the item is removed.
   *
   * @param session the HTTP session containing the user ID
   * @param listingId the ID of the listing to update
   * @param qty the new quantity
   * @return the updated {@link CartResponseDto}
   */
  @Transactional
  public CartResponseDto updateQuantity(HttpSession session, Long listingId, int qty) {
    Cart cart = getOrCreateActiveCartEntity(session);

    if (qty <= 0) {
      return removeItem(session, listingId);
    }

    CartItem item =
        cartItemRepository
            .findByCart_CartIdAndListing_ListingId(cart.getCartId(), listingId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart"));

    item.setQuantity(qty);
    cartItemRepository.save(item);

    return toCartResponseDto(cart);
  }

  /**
   * Removes an item from the cart and returns the updated cart as a CartResponseDto. If the cart
   * becomes empty, the store reference is cleared.
   *
   * @param session the HTTP session containing the user ID
   * @param listingId the ID of the listing to remove
   * @return the updated {@link CartResponseDto}
   */
  @Transactional
  public CartResponseDto removeItem(HttpSession session, Long listingId) {
    Cart cart = getOrCreateActiveCartEntity(session);

    CartItem item =
        cartItemRepository
            .findByCart_CartIdAndListing_ListingId(cart.getCartId(), listingId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart"));

    cartItemRepository.delete(item);

    // If cart is now empty, clear the store
    List<CartItem> remainingItems = cartItemRepository.findByCart_CartId(cart.getCartId());
    if (remainingItems.isEmpty()) {
      cart.setStore(null);
      cartRepository.save(cart);
    }

    return toCartResponseDto(cart);
  }

  /**
   * Clears all items from the cart and returns the updated cart as a CartResponseDto.
   *
   * @param session the HTTP session containing the user ID
   * @return the updated {@link CartResponseDto}
   */
  @Transactional
  public CartResponseDto clearCart(HttpSession session) {
    Cart cart = getOrCreateActiveCartEntity(session);

    cartItemRepository.deleteByCart_CartId(cart.getCartId());
    cart.setStore(null);
    cartRepository.save(cart);

    return toCartResponseDto(cart);
  }

  /**
   * Helper method to get or create the active Cart entity (not DTO).
   *
   * @param session the HTTP session containing the user ID
   * @return the active {@link Cart} entity
   * @throws ResponseStatusException if user is not authenticated or is not a consumer
   */
  private Cart getOrCreateActiveCartEntity(HttpSession session) {
    Long consumerId = (Long) session.getAttribute("USER_ID");
    String role = (String) session.getAttribute("USER_ROLE");

    if (consumerId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    if (!"CONSUMER".equals(role)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only consumers can have carts");
    }

    ConsumerProfile consumer =
        consumerProfileRepository
            .findById(consumerId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consumer not found"));

    return cartRepository
        .findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(consumerId, "ACTIVE")
        .orElseGet(
            () -> {
              Cart newCart = new Cart();
              newCart.setConsumer(consumer);
              newCart.setStatus("ACTIVE");
              return cartRepository.save(newCart);
            });
  }

  /**
   * Maps a Cart entity to a CartResponseDto for API responses. Calculates subtotal and total from
   * all cart items.
   *
   * @param cart the Cart entity to map
   * @return the {@link CartResponseDto} representation
   */
  public CartResponseDto toCartResponseDto(Cart cart) {
    CartResponseDto dto = new CartResponseDto();
    dto.setCartId(cart.getCartId());
    dto.setSupplierId(cart.getStore() != null ? cart.getStore().getStoreId() : null);

    List<CartItem> items = cartItemRepository.findByCart_CartId(cart.getCartId());
    List<CartResponseDto.CartItemDto> itemDtos =
        items.stream().map(this::toCartItemDto).collect(Collectors.toList());
    dto.setItems(itemDtos);

    // Calculate subtotal and total from line totals
    BigDecimal subtotal =
        itemDtos.stream()
            .map(CartResponseDto.CartItemDto::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    dto.setSubtotal(subtotal);
    dto.setTotal(subtotal);

    // Calculate totalSavings: sum of (originalPrice - unitPrice) * qty for each item
    BigDecimal totalSavings =
        itemDtos.stream()
            .map(
                i -> {
                  if (i.getOriginalPrice() != null && i.getUnitPrice() != null) {
                    return i.getOriginalPrice()
                        .subtract(i.getUnitPrice())
                        .multiply(BigDecimal.valueOf(i.getQty()));
                  } else {
                    return BigDecimal.ZERO;
                  }
                })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    dto.setTotalSavings(totalSavings);

    return dto;
  }

  /**
   * Maps a CartItem entity to a CartResponseDto.CartItemDto. Extracts the first photo from the
   * listing's photos if available.
   *
   * @param item the CartItem entity to map
   * @return the {@link CartResponseDto.CartItemDto} representation
   */
  public CartResponseDto.CartItemDto toCartItemDto(CartItem item) {
    CartResponseDto.CartItemDto dto = new CartResponseDto.CartItemDto();
    dto.setListingId(item.getListing().getListingId());
    dto.setTitle(item.getListing().getTitle());
    dto.setUnitPrice(item.getListing().getRescuePrice());
    dto.setQty(item.getQuantity());
    dto.setLineTotal(
        item.getListing().getRescuePrice().multiply(BigDecimal.valueOf(item.getQuantity())));

    // Extract first photo URL if photos list exists and is not empty
    dto.setImageUrl(
        item.getListing().getPhotos() != null && !item.getListing().getPhotos().isEmpty()
            ? item.getListing().getPhotos().get(0).getPhotoUrl()
            : null);

    // Add pickup window and store name
    dto.setPickupStart(
        item.getListing().getPickupStart() != null
            ? item.getListing().getPickupStart().toString()
            : null);
    dto.setPickupEnd(
        item.getListing().getPickupEnd() != null
            ? item.getListing().getPickupEnd().toString()
            : null);
    dto.setStoreName(
        item.getListing().getStore() != null ? item.getListing().getStore().getStoreName() : null);

    // Add originalPrice and savingsLabel
    dto.setOriginalPrice(item.getListing().getOriginalPrice());
    BigDecimal savingsAmount =
        item.getListing().getOriginalPrice().subtract(item.getListing().getRescuePrice());
    dto.setSavingsLabel(
        savingsAmount.compareTo(BigDecimal.ZERO) > 0
            ? "Worth $" + item.getListing().getOriginalPrice().intValue() + "+"
            : null);

    return dto;
  }
}
