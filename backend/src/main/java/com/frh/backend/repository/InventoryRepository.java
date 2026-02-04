package com.frh.backend.repository;

import com.frh.backend.Model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

  /* listing display. */
  Optional<Inventory> findByListingListingId(Long listingId);

  /*
   * Called inside the accept-order transaction so no other thread
   * can decrement the same row simultaneously (oversell guard).
   */

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT i FROM Inventory i WHERE i.listing.listingId = :listingId")
  Optional<Inventory> findByListingIdForUpdate(@Param("listingId") Long listingId);
}