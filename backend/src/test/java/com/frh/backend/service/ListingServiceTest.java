package com.frh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frh.backend.Model.FoodCategory;
import com.frh.backend.Model.Inventory;
import com.frh.backend.Model.Listing;
import com.frh.backend.Model.ListingFoodCategory;
import com.frh.backend.Model.ListingPhoto;
import com.frh.backend.Model.Store;
import com.frh.backend.Model.StoreType;
import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.dto.ListingCategoryWeightDTO;
import com.frh.backend.dto.ListingDTO;
import com.frh.backend.repository.FoodCategoryRepository;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.StoreRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

  @Mock private ListingRepository listingRepository;

  @Mock private FoodCategoryRepository foodCategoryRepository;

  @Mock private StoreRepository storeRepository;

  @InjectMocks private ListingService listingService;

  @Test
  void getAllActiveListings_mapsListingToDto() {
    Listing listing = buildListing(1L, "Croissant", "Bakery", LocalDateTime.now().plusHours(2));

    when(listingRepository.findAllActiveListingsWithDetails()).thenReturn(List.of(listing));

    List<ListingDTO> results = listingService.getAllActiveListings();

    assertEquals(1, results.size());
    ListingDTO dto = results.get(0);
    assertEquals(1L, dto.getListingId());
    assertEquals("Croissant", dto.getTitle());
    assertEquals("Bakery", dto.getCategory());
    assertEquals(List.of("https://img/1.png", "https://img/2.png"), dto.getPhotoUrls());
    assertEquals(new BigDecimal("6.00"), dto.getSavingsAmount());
    assertEquals("Worth $12+", dto.getSavingsLabel());
    assertTrue(dto.getTimeRemaining().contains("left"));
  }

  @Test
  void getAllActiveListings_expiredAndNullPickupEnd() {
    Listing expiredListing = buildListing(2L, "Soup", "Cafe", LocalDateTime.now().minusMinutes(5));
    Listing nullPickupEndListing = buildListing(3L, "Salad", "Cafe", null);

    when(listingRepository.findAllActiveListingsWithDetails())
        .thenReturn(List.of(expiredListing, nullPickupEndListing));

    List<ListingDTO> results = listingService.getAllActiveListings();

    assertEquals("Expired", results.get(0).getTimeRemaining());
    assertEquals("N/A", results.get(1).getTimeRemaining());
  }

  @Test
  void getNearbyListings_usesDefaultRadiusWhenMissing() {
    when(listingRepository.findNearbyListings(1.30, 103.80, 5.0)).thenReturn(List.of());

    List<ListingDTO> results = listingService.getNearbyListings(1.30, 103.80, null);

    assertNotNull(results);
    verify(listingRepository).findNearbyListings(1.30, 103.80, 5.0);
  }

  @Test
  void getNearbyListings_usesDefaultRadiusWhenNonPositive() {
    when(listingRepository.findNearbyListings(1.30, 103.80, 5.0)).thenReturn(List.of());

    listingService.getNearbyListings(1.30, 103.80, 0.0);

    verify(listingRepository).findNearbyListings(1.30, 103.80, 5.0);
  }

  @Test
  void getNearbyListings_usesProvidedRadius() {
    Listing listing = buildListing(4L, "Noodles", "Hawker", LocalDateTime.now().plusMinutes(30));
    when(listingRepository.findNearbyListings(1.31, 103.81, 3.5)).thenReturn(List.of(listing));

    List<ListingDTO> results = listingService.getNearbyListings(1.31, 103.81, 3.5);

    assertEquals(1, results.size());
    verify(listingRepository).findNearbyListings(1.31, 103.81, 3.5);
  }

  @Test
  void getListingsByCategory_filtersCorrectly() {
    Listing bakery = buildListing(5L, "Bread Box", "Bakery", LocalDateTime.now().plusHours(1));
    Listing unknown = buildListing(6L, "Mixed Meal", null, LocalDateTime.now().plusHours(1));

    when(listingRepository.findAllActiveListingsWithDetails()).thenReturn(List.of(bakery, unknown));

    List<ListingDTO> bakeryOnly = listingService.getListingsByCategory("Bakery");
    List<ListingDTO> all = listingService.getListingsByCategory("All");

    assertEquals(1, bakeryOnly.size());
    assertEquals(5L, bakeryOnly.get(0).getListingId());
    assertEquals(2, all.size());
  }

  @Test
  void getAllActiveListings_handlesMissingOptionalRelations() {
    Listing listing = new Listing();
    listing.setListingId(7L);
    listing.setTitle("Simple Item");
    listing.setDescription("No relations");
    listing.setOriginalPrice(new BigDecimal("10.00"));
    listing.setRescuePrice(new BigDecimal("4.00"));
    listing.setPickupStart(LocalDateTime.now().plusMinutes(5));
    listing.setPickupEnd(LocalDateTime.now().plusMinutes(20));
    listing.setExpiryAt(LocalDateTime.now().plusHours(1));
    listing.setStatus("ACTIVE");
    listing.setStore(null);
    listing.setInventory(null);
    listing.setPhotos(List.of());
    listing.setListingFoodCategories(null);

    when(listingRepository.findAllActiveListingsWithDetails()).thenReturn(List.of(listing));

    List<ListingDTO> results = listingService.getAllActiveListings();

    assertEquals(1, results.size());
    ListingDTO dto = results.get(0);
    assertEquals(7L, dto.getListingId());
    assertEquals(0, dto.getQtyAvailable());
    assertEquals(0, dto.getQtyReserved());
    assertNull(dto.getStoreId());
    assertNull(dto.getCategory());
    assertNotNull(dto.getTimeRemaining());
  }

  @Test
  void createListing_withCategoryWeights_skipsInvalidRowsAndUsesDefaultQty() {
    ListingDTO dto = baseCreateDto();
    dto.setQtyAvailable(null);

    ListingCategoryWeightDTO nullCategory = new ListingCategoryWeightDTO();
    nullCategory.setCategoryId(null);
    nullCategory.setWeightKg(new BigDecimal("0.500"));

    ListingCategoryWeightDTO validWeighted = new ListingCategoryWeightDTO();
    validWeighted.setCategoryId(1L);
    validWeighted.setWeightKg(new BigDecimal("1.200"));

    ListingCategoryWeightDTO missingCategory = new ListingCategoryWeightDTO();
    missingCategory.setCategoryId(2L);
    missingCategory.setWeightKg(new BigDecimal("2.000"));

    ListingCategoryWeightDTO validNullWeight = new ListingCategoryWeightDTO();
    validNullWeight.setCategoryId(3L);
    validNullWeight.setWeightKg(null);

    dto.setCategoryWeights(List.of(nullCategory, validWeighted, missingCategory, validNullWeight));

    FoodCategory c1 = new FoodCategory();
    c1.setId(1L);
    c1.setName("Meat");
    FoodCategory c3 = new FoodCategory();
    c3.setId(3L);
    c3.setName("Vegetable");

    Store store = storeForCreate(90L, "Bakery");

    when(foodCategoryRepository.findAllById(anyList())).thenReturn(List.of(c1, c3));
    when(storeRepository.getReferenceById(90L)).thenReturn(store);
    when(listingRepository.save(any(Listing.class)))
        .thenAnswer(
            invocation -> {
              Listing listing = invocation.getArgument(0);
              listing.setListingId(900L);
              return listing;
            });

    ListingDTO result = listingService.createListing(dto, 90L);

    ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
    verify(listingRepository).save(listingCaptor.capture());
    Listing saved = listingCaptor.getValue();

    assertEquals(1, saved.getAvailableQty());
    assertEquals(new BigDecimal("1.200"), saved.getEstimatedWeightKg());
    assertEquals(2, saved.getListingFoodCategories().size());

    assertEquals(900L, result.getListingId());
    assertEquals(List.of(1L, 3L), result.getCategoryIds());
    assertEquals(List.of("Meat", "Vegetable"), result.getCategoryNames());
    assertEquals("Bakery", result.getCategory());
  }

  @Test
  void createListing_withCategoryIds_createsLinksWithNullWeight() {
    ListingDTO dto = baseCreateDto();
    dto.setQtyAvailable(4);
    dto.setCategoryWeights(null);
    dto.setCategoryIds(List.of(5L, 6L));

    FoodCategory c5 = new FoodCategory();
    c5.setId(5L);
    c5.setName("Bread");
    FoodCategory c6 = new FoodCategory();
    c6.setId(6L);
    c6.setName("Beverage");

    Store store = storeForCreate(91L, null);

    when(foodCategoryRepository.findAllById(dto.getCategoryIds())).thenReturn(List.of(c5, c6));
    when(storeRepository.getReferenceById(91L)).thenReturn(store);
    when(listingRepository.save(any(Listing.class)))
        .thenAnswer(
            invocation -> {
              Listing listing = invocation.getArgument(0);
              listing.setListingId(901L);
              return listing;
            });

    ListingDTO result = listingService.createListing(dto, 91L);

    ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
    verify(listingRepository).save(listingCaptor.capture());
    Listing saved = listingCaptor.getValue();

    assertEquals(4, saved.getAvailableQty());
    assertEquals(2, saved.getListingFoodCategories().size());
    for (ListingFoodCategory link : saved.getListingFoodCategories()) {
      assertNull(link.getWeightKg());
    }

    assertEquals(901L, result.getListingId());
    assertEquals("Unknown", result.getCategory());
  }

  @Test
  void createListing_storeIdNull_throwsIllegalArgumentException() {
    ListingDTO dto = baseCreateDto();
    dto.setCategoryWeights(null);
    dto.setCategoryIds(List.of());

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> listingService.createListing(dto, null));

    assertEquals("Store is required", ex.getMessage());
  }

  private static ListingDTO baseCreateDto() {
    ListingDTO dto = new ListingDTO();
    dto.setTitle("Set Meal");
    dto.setDescription("Dinner set");
    dto.setOriginalPrice(new BigDecimal("15.00"));
    dto.setRescuePrice(new BigDecimal("8.00"));
    dto.setPickupStart(LocalDateTime.now().plusHours(1));
    dto.setPickupEnd(LocalDateTime.now().plusHours(2));
    dto.setExpiryAt(LocalDateTime.now().plusHours(5));
    return dto;
  }

  private static Store storeForCreate(Long storeId, String typeName) {
    Store store = new Store();
    store.setStoreId(storeId);
    store.setStoreName("Store " + storeId);
    store.setDescription("Description");
    store.setAddressLine("Addr");
    store.setPostalCode("000000");
    store.setLat(new BigDecimal("1.3000000"));
    store.setLng(new BigDecimal("103.8000000"));
    store.setPickupInstructions("Counter");
    store.setOpeningHours("08:00-20:00");

    SupplierProfile supplier = new SupplierProfile();
    if (typeName != null) {
      StoreType type = new StoreType();
      type.setTypeName(typeName);
      supplier.setStoreType(type);
    }
    store.setSupplierProfile(supplier);
    return store;
  }

  private static Listing buildListing(
      Long id, String title, String storeTypeName, LocalDateTime pickupEnd) {
    SupplierProfile supplier = new SupplierProfile();
    if (storeTypeName != null) {
      StoreType storeType = new StoreType();
      storeType.setTypeName(storeTypeName);
      supplier.setStoreType(storeType);
    }

    Store store = new Store();
    store.setStoreId(20L + id);
    store.setStoreName("Store " + id);
    store.setDescription("Description " + id);
    store.setAddressLine("Address " + id);
    store.setPostalCode("00000" + id);
    store.setLat(new BigDecimal("1.3000000"));
    store.setLng(new BigDecimal("103.8000000"));
    store.setPickupInstructions("Pick at counter");
    store.setOpeningHours("09:00-18:00");
    store.setSupplierProfile(supplier);

    Listing listing = new Listing();
    listing.setListingId(id);
    listing.setStore(store);
    listing.setTitle(title);
    listing.setDescription("Fresh food");
    listing.setOriginalPrice(new BigDecimal("12.00"));
    listing.setRescuePrice(new BigDecimal("6.00"));
    listing.setPickupStart(LocalDateTime.now().minusMinutes(30));
    listing.setPickupEnd(pickupEnd);
    listing.setExpiryAt(LocalDateTime.now().plusHours(4));
    listing.setStatus("ACTIVE");

    Inventory inventory = new Inventory();
    inventory.setListing(listing);
    inventory.setQtyAvailable(8);
    inventory.setQtyReserved(1);
    listing.setInventory(inventory);

    ListingPhoto photo2 = new ListingPhoto();
    photo2.setSortOrder(2);
    photo2.setPhotoUrl("https://img/2.png");
    ListingPhoto photo1 = new ListingPhoto();
    photo1.setSortOrder(1);
    photo1.setPhotoUrl("https://img/1.png");
    listing.setPhotos(List.of(photo2, photo1));

    return listing;
  }
}
