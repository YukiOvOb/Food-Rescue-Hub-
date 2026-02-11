package com.frh.backend.repository;

import com.frh.backend.model.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  List<CartItem> findByCart_CartId(Long cartId);

  Optional<CartItem> findByCart_CartIdAndListing_ListingId(Long cartId, Long listingId);

  void deleteByCart_CartId(Long cartId);
}
