package com.frh.backend.controller;

import com.frh.backend.model.Inventory;
import com.frh.backend.model.Listing;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.service.InventoryService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/* the supplier's inventory page.*/

@Controller
@RequestMapping("/supplier/inventory")
public class SupplierInventoryPageController {

  @Autowired private InventoryService inventoryService;

  @Autowired private ListingRepository listingRepository;

  // Render page
  /*
   * Fetches every listing for the given store and attaches its live
   * inventory so the Thymeleaf template can render the table.
   */
  @GetMapping("/{storeId}")
  public String showInventory(@PathVariable Long storeId, Model model) {

    // to Fetch all listings

    List<Listing> listings = listingRepository.findByStoreStoreId(storeId);

    // Build a lightweight view-model: pair each listing with its Inventory
    List<InventoryRowDTO> rows = new ArrayList<>();
    for (Listing listing : listings) {
      Inventory inv = inventoryService.getInventory(listing.getListingId());
      rows.add(new InventoryRowDTO(listing, inv));
    }

    model.addAttribute("storeId", storeId);
    model.addAttribute("rows", rows);

    return "supplier/inventory-manage";
    // templates/supplier/inventory-manage.html
  }

  // Adjust stock
  @PostMapping("/{storeId}/adjust")
  public String adjustStock(
      @PathVariable Long storeId,
      @RequestParam Long listingId,
      @RequestParam int delta,
      RedirectAttributes ra) {
    try {
      inventoryService.adjustInventory(listingId, delta);
      ra.addFlashAttribute("successMsg", "Stock for listing #" + listingId + " updated.");
    } catch (Exception ex) {
      ra.addFlashAttribute("errorMsg", ex.getMessage());
    }
    return "redirect:/supplier/inventory/" + storeId;
  }

  // Tiny inner DTO – avoids creating a separate file
  /**
   * Pairs a Listing with its Inventory for the Thymeleaf table row. Kept package-private; Thymeleaf
   * accesses fields via getters.
   */
  public static class InventoryRowDTO {
    private final Listing listing;
    private final Inventory inventory;

    public InventoryRowDTO(Listing listing, Inventory inventory) {
      this.listing = listing;
      this.inventory = inventory;
    }

    public Listing getListing() {
      return listing;
    }

    public Inventory getInventory() {
      return inventory;
    }
  }
}

/*
 * just for my reference - roughly
 * the given Routes
 * GET /supplier/inventory/{storeId} – render all the listings + stock
 * POST /supplier/inventory/{storeId}/adjust – manual stock adjustment
 */
