package com.frh.backend.service;

import com.frh.backend.Model.*;
import com.frh.backend.exception.CrossStoreException;
import com.frh.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpSession;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ListingRepository listingRepository;
    private final ConsumerProfileRepository consumerProfileRepository;

    /**
     * Retrieves the active cart for the current session user. If no active cart exists,
     * a new one is created and returned.
     *
     * @param session the HTTP session containing the user ID
     * @return the active {@link Cart} associated with the current user
     * @throws ResponseStatusException if the user is not authenticated or if the consumer does not exist
     */
    public Cart getOrCreateActiveCart(HttpSession session) {
        Long consumerId = (Long) session.getAttribute("USER_ID");
        String role = (String) session.getAttribute("USER_ROLE");
        
        if (consumerId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        
        if (!"CONSUMER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only consumers can have carts");
        }

        ConsumerProfile consumer = consumerProfileRepository.findById(consumerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consumer not found"));

        return cartRepository.findByConsumer_ConsumerIdAndStatus(consumerId, "ACTIVE")
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setConsumer(consumer);
                    cart.setStatus("ACTIVE");
                    return cartRepository.save(cart);
                });
    }

    public List<CartItem> getCartItems(Long cartId) {
        return cartItemRepository.findByCart_CartId(cartId);
    }

    @Transactional
    public Cart addItem(HttpSession session, Long listingId, int qty) {
        if (qty <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        Cart cart = getOrCreateActiveCart(session);

        if (cart.getStore() != null && !cart.getStore().getStoreId().equals(listing.getStore().getStoreId())) {
            throw new CrossStoreException("Cannot add items from multiple stores to the same cart", cart.getStore().getStoreId());
        }

        cart.setStore(listing.getStore());
        cartRepository.save(cart);
        
        Optional<CartItem> existingItem = cartItemRepository.findByCart_CartIdAndListing_ListingId(cart.getCartId(), listingId);

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

        return cart;
    }
    
    @Transactional
    public Cart updateQuantity(HttpSession session, Long listingId, int qty) {
        Cart cart = getOrCreateActiveCart(session);
        
        if (qty <= 0) {
            return removeItem(session, listingId);
        }
        
        CartItem item = cartItemRepository.findByCart_CartIdAndListing_ListingId(cart.getCartId(), listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart"));
        
        item.setQuantity(qty);
        cartItemRepository.save(item);
        
        return cart;
    }
    
    @Transactional
    public Cart removeItem(HttpSession session, Long listingId) {
        Cart cart = getOrCreateActiveCart(session);
        
        CartItem item = cartItemRepository.findByCart_CartIdAndListing_ListingId(cart.getCartId(), listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart"));
        
        cartItemRepository.delete(item);
        
        // If cart is now empty, clear the store
        List<CartItem> remainingItems = cartItemRepository.findByCart_CartId(cart.getCartId());
        if (remainingItems.isEmpty()) {
            cart.setStore(null);
            cartRepository.save(cart);
        }
        
        return cart;
    }
    
    @Transactional
    public Cart clearCart(HttpSession session) {
        Cart cart = getOrCreateActiveCart(session);
        
        cartItemRepository.deleteByCart_CartId(cart.getCartId());
        cart.setStore(null);
        cartRepository.save(cart);
        
        return cart;
    }
}