package com.frh.backend.service;

import com.frh.backend.dto.ListingCategoryWeightDto;
import com.frh.backend.dto.ListingDto;
import com.frh.backend.model.Listing;
import com.frh.backend.model.ListingFoodCategory;
import com.frh.backend.model.ListingPhoto;
import com.frh.backend.repository.FoodCategoryRepository;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.StoreRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListingService {

  @Autowired private ListingRepository listingRepository;

  @Autowired private FoodCategoryRepository foodCategoryRepository;

  @Autowired private StoreRepository storeRepository;

  /** Get all active listings with available inventory */
  @Transactional(readOnly = true)
  public List<ListingDto> getAllActiveListings() {
    List<Listing> listings = listingRepository.findAllActiveListingsWithDetails();
    return listings.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  /**
   * Get nearby listings based on user location
   *
   * @param lat User's latitude
   * @param lng User's longitude
   * @param radius Search radius in kilometers (default: 5km)
   */
  @Transactional(readOnly = true)
  public List<ListingDto> getNearbyListings(Double lat, Double lng, Double radius) {
    if (radius == null || radius <= 0) {
      radius = 5.0; // Default 5km radius
    }
    List<Listing> listings = listingRepository.findNearbyListings(lat, lng, radius);
    return listings.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  /** Get listings for a supplier (DTO-safe) */
  @Transactional(readOnly = true)
  public List<ListingDto> getListingsBySupplier(Long supplierId) {
    List<Listing> listings = listingRepository.findByStore_SupplierProfile_SupplierId(supplierId);
    return listings.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  /** Filter listings by category */
  @Transactional(readOnly = true)
  public List<ListingDto> getListingsByCategory(String category) {
    List<Listing> allListings = listingRepository.findAllActiveListingsWithDetails();
    return allListings.stream()
        .filter(
            listing -> {
              String storeCategory = getStoreCategory(listing);
              return category.equalsIgnoreCase("All") || category.equalsIgnoreCase(storeCategory);
            })
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /** Convert Listing entity to ListingDto */
  private ListingDto convertToDto(Listing listing) {
    ListingDto dto = new ListingDto();

    // Basic listing info
    dto.setListingId(listing.getListingId());
    dto.setTitle(listing.getTitle());
    dto.setDescription(listing.getDescription());
    dto.setOriginalPrice(listing.getOriginalPrice());
    dto.setRescuePrice(listing.getRescuePrice());
    dto.setPickupStart(listing.getPickupStart());
    dto.setPickupEnd(listing.getPickupEnd());
    dto.setExpiryAt(listing.getExpiryAt());
    dto.setStatus(listing.getStatus());

    // Store info
    if (listing.getStore() != null) {
      dto.setStoreId(listing.getStore().getStoreId());
      dto.setStoreName(listing.getStore().getStoreName());
      dto.setStoreDescription(listing.getStore().getDescription());
      dto.setAddressLine(listing.getStore().getAddressLine());
      dto.setPostalCode(listing.getStore().getPostalCode());
      dto.setLat(listing.getStore().getLat());
      dto.setLng(listing.getStore().getLng());
      dto.setPickupInstructions(listing.getStore().getPickupInstructions());
      dto.setOpeningHours(listing.getStore().getOpeningHours());

      // Category from StoreType
      dto.setCategory(getStoreCategory(listing));
    }

    // Inventory (default to 0 when inventory or quantity fields are missing)
    Integer qtyAvailable = 0;
    Integer qtyReserved = 0;
    if (listing.getInventory() != null) {
      if (listing.getInventory().getQtyAvailable() != null) {
        qtyAvailable = listing.getInventory().getQtyAvailable();
      }
      if (listing.getInventory().getQtyReserved() != null) {
        qtyReserved = listing.getInventory().getQtyReserved();
      }
    }
    dto.setQtyAvailable(qtyAvailable);
    dto.setQtyReserved(qtyReserved);

    // Photos
    if (listing.getPhotos() != null && !listing.getPhotos().isEmpty()) {
      List<String> photoUrls =
          listing.getPhotos().stream()
              .sorted((p1, p2) -> Integer.compare(p1.getSortOrder(), p2.getSortOrder()))
              .map(ListingPhoto::getPhotoUrl)
              .collect(Collectors.toList());
      dto.setPhotoUrls(photoUrls);
    }

    // Map the CO2 categories to the DTO
    if (listing.getListingFoodCategories() != null) {
      dto.setCategoryIds(
          listing.getListingFoodCategories().stream()
              .map(lfc -> lfc.getCategory().getId())
              .collect(Collectors.toList()));

      dto.setCategoryNames(
          listing.getListingFoodCategories().stream()
              .map(lfc -> lfc.getCategory().getName())
              .collect(Collectors.toList()));

      dto.setCategoryWeights(
          listing.getListingFoodCategories().stream()
              .map(
                  lfc -> {
                    ListingCategoryWeightDto w = new ListingCategoryWeightDto();
                    w.setCategoryId(lfc.getCategory().getId());
                    w.setCategoryName(lfc.getCategory().getName());
                    w.setWeightKg(lfc.getWeightKg());
                    return w;
                  })
              .collect(Collectors.toList()));
    }
    dto.setEstimatedWeightKg(listing.getEstimatedWeightKg());

    // Calculated fields
    dto.setTimeRemaining(calculateTimeRemaining(listing.getPickupEnd()));
    dto.setSavingsAmount(listing.getOriginalPrice().subtract(listing.getRescuePrice()));
    dto.setSavingsLabel("Worth $" + listing.getOriginalPrice().intValue() + "+");

    return dto;
  }

  /** Get store category from listing */
  private String getStoreCategory(Listing listing) {
    if (listing.getStore() != null
        && listing.getStore().getSupplierProfile() != null
        && listing.getStore().getSupplierProfile().getStoreType() != null) {
      return listing.getStore().getSupplierProfile().getStoreType().getTypeName();
    }
    return "Unknown";
  }

  /** Calculate time remaining until pickup end */
  private String calculateTimeRemaining(LocalDateTime pickupEnd) {
    if (pickupEnd == null) {
      return "N/A";
    }

    LocalDateTime now = LocalDateTime.now();
    if (now.isAfter(pickupEnd)) {
      return "Expired";
    }

    Duration duration = Duration.between(now, pickupEnd);
    long hours = duration.toHours();
    long minutes = duration.toMinutes() % 60;

    if (hours > 0) {
      return hours + "h " + minutes + "m left";
    } else {
      return minutes + "m left";
    }
  }

  @Transactional
  public ListingDto createListing(ListingDto dto, Long storeId) {
    Listing listing = new Listing();

    // 1. Map fields from DTO
    applyListingFields(listing, dto);

    // 2. Map the Food Categories for CO2 tracking (per-category weights)
    applyFoodCategories(listing, dto, true);

    // 3. Initialize Inventory via your existing helper
    listing.setAvailableQty(dto.getQtyAvailable() != null ? dto.getQtyAvailable() : 1);

    // 4. Link to Store
    if (storeId == null) {
      throw new IllegalArgumentException("Store is required");
    }
    listing.setStore(storeRepository.getReferenceById(storeId));

    Listing savedListing = listingRepository.save(listing);
    return convertToDto(savedListing);
  }

  @Transactional
  public ListingDto updateListing(Long listingId, ListingDto dto) {
    Listing listing =
        listingRepository
            .findById(listingId)
            .orElseThrow(
                () -> new IllegalArgumentException("Listing not found with id: " + listingId));

    applyListingFields(listing, dto);
    applyFoodCategories(listing, dto, false);

    Listing savedListing = listingRepository.save(listing);
    return convertToDto(savedListing);
  }

  private void applyListingFields(Listing listing, ListingDto dto) {
    listing.setTitle(dto.getTitle());
    listing.setDescription(dto.getDescription());
    listing.setOriginalPrice(dto.getOriginalPrice());
    listing.setRescuePrice(dto.getRescuePrice());
    listing.setPickupStart(dto.getPickupStart());
    listing.setPickupEnd(dto.getPickupEnd());
    listing.setExpiryAt(dto.getExpiryAt());
    listing.setEstimatedWeightKg(dto.getEstimatedWeightKg());
  }

  private void applyFoodCategories(Listing listing, ListingDto dto, boolean clearWhenMissing) {
    boolean hasCategoryWeights = dto.getCategoryWeights() != null;
    boolean hasCategoryIds = dto.getCategoryIds() != null;

    if (!hasCategoryWeights && !hasCategoryIds) {
      if (clearWhenMissing) {
        listing.getListingFoodCategories().clear();
      }
      return;
    }

    listing.getListingFoodCategories().clear();

    if (dto.getCategoryWeights() != null && !dto.getCategoryWeights().isEmpty()) {
      List<Long> ids =
          dto.getCategoryWeights().stream()
              .map(ListingCategoryWeightDto::getCategoryId)
              .filter(id -> id != null)
              .distinct()
              .collect(Collectors.toList());
      Map<Long, com.frh.backend.model.FoodCategory> categoryMap =
          foodCategoryRepository.findAllById(ids).stream()
              .collect(Collectors.toMap(com.frh.backend.model.FoodCategory::getId, c -> c));

      BigDecimal totalWeight = BigDecimal.ZERO;

      for (ListingCategoryWeightDto item : dto.getCategoryWeights()) {
        if (item.getCategoryId() == null) continue;
        com.frh.backend.model.FoodCategory category = categoryMap.get(item.getCategoryId());
        if (category == null) continue;

        ListingFoodCategory link = new ListingFoodCategory();
        link.setListing(listing);
        link.setCategory(category);
        link.setWeightKg(item.getWeightKg());
        listing.getListingFoodCategories().add(link);

        if (item.getWeightKg() != null) {
          totalWeight = totalWeight.add(item.getWeightKg());
        }
      }

      if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
        listing.setEstimatedWeightKg(totalWeight);
      }
      return;
    }

    if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
      List<com.frh.backend.model.FoodCategory> categories =
          foodCategoryRepository.findAllById(dto.getCategoryIds());
      for (com.frh.backend.model.FoodCategory category : categories) {
        ListingFoodCategory link = new ListingFoodCategory();
        link.setListing(listing);
        link.setCategory(category);
        link.setWeightKg(null);
        listing.getListingFoodCategories().add(link);
      }
    }
  }
}
