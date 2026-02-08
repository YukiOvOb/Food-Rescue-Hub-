package com.frh.backend.service;

import com.frh.backend.Model.Inventory;
import com.frh.backend.Model.Listing;
import com.frh.backend.Model.ListingPhoto;
import com.frh.backend.Model.Store;
import com.frh.backend.Model.StoreType;
import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.dto.ListingDTO;
import com.frh.backend.repository.ListingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @InjectMocks
    private ListingService listingService;

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

    private static Listing buildListing(Long id, String title, String storeTypeName, LocalDateTime pickupEnd) {
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
