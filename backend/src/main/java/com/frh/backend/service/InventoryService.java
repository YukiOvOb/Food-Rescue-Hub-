package com.frh.backend.service;

import com.frh.backend.Model.Inventory;
import com.frh.backend.exception.InsufficientStockException;
import com.frh.backend.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

  @Autowired
  private InventoryRepository inventoryRepository;

  // Read-only stock check (no lock)
  /**
   * Returns {@code true} when the listing has at least {@code qty} units.
   * Called early in createOrder to fail fast; the binding guard is in
   * {@link #decrementStock}.
   */
  
  @Transactional(readOnly = true)
  public boolean checkStock(Long listingId, int qty) {
    Inventory inv = inventoryRepository
        .findByListingListingId(listingId)
        .orElseThrow(() -> new RuntimeException("Inventory not found for listing " + listingId));
    return inv.getQtyAvailable() >= qty;
  }

  // Locked decrement (called on ACCEPT)
  /**
   * Acquires a pessimistic lock on the inventory row, re-checks stock,
   * then decrements {@code qtyAvailable}.
   *
   * @throws InsufficientStockException if stock dropped between the first
   *                                    check and this locked re-check (race
   *                                    condition caught).
   */

  @Transactional
  public Inventory decrementStock(Long listingId, int qty) {
    // Lock the row – any other thread trying the same listing will block here
    Inventory inv = inventoryRepository
        .findByListingIdForUpdate(listingId)
        .orElseThrow(() -> new RuntimeException("Inventory not found for listing " + listingId));

    // Double-check under lock – this is the real oversell guard
    if (inv.getQtyAvailable() < qty) {
      throw new InsufficientStockException(listingId, qty, inv.getQtyAvailable());
    }

    inv.setQtyAvailable(inv.getQtyAvailable() - qty);
    return inventoryRepository.save(inv);
  }

  // Locked restore (called on REJECT or CANCEL after an ACCEPT)
  /**
   * Adds units back to {@code qtyAvailable}.
   * Used when a previously-accepted order is later cancelled.
   */

  @Transactional
  public Inventory restoreStock(Long listingId, int qty) {
    Inventory inv = inventoryRepository
        .findByListingIdForUpdate(listingId)
        .orElseThrow(() -> new RuntimeException("Inventory not found for listing " + listingId));

    inv.setQtyAvailable(inv.getQtyAvailable() + qty);
    return inventoryRepository.save(inv);
  }

  // supplier restocks or removes spoiled
  /**
   * Applies a signed {@code delta} to {@code qtyAvailable}.
   * positive delta to restock
   * negative delta to remove (e.g. spoiled goods)
   * 
   * @throws IllegalArgumentException when the resulting qty would be < 0.
   */

  @Transactional
  public Inventory adjustInventory(Long listingId, int delta) {
    Inventory inv = inventoryRepository
        .findByListingIdForUpdate(listingId)
        .orElseThrow(() -> new RuntimeException("Inventory not found for listing " + listingId));

    int newQty = inv.getQtyAvailable() + delta;
    if (newQty < 0) {
      throw new IllegalArgumentException(
          "Cannot adjust inventory below zero. Current: " +
              inv.getQtyAvailable() + ", delta: " + delta);
    }

    inv.setQtyAvailable(newQty);
    return inventoryRepository.save(inv);
  }

  @Transactional(readOnly = true)
  public Inventory getInventory(Long listingId) {
    return inventoryRepository
        .findByListingListingId(listingId)
        .orElseThrow(() -> new RuntimeException("Inventory not found for listing " + listingId));
  }
}
