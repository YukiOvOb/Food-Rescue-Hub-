package com.frh.backend;

import com.frh.backend.Model.*;
import com.frh.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final StoreRepository storeRepository;
    private final ListingRepository listingRepository;
    private final InventoryRepository inventoryRepository;
    private final DietaryTagRepository dietaryTagRepository;

    public DataInitializer(UserRepository userRepository,
                           SupplierRepository supplierRepository,
                           StoreRepository storeRepository,
                           ListingRepository listingRepository,
                           InventoryRepository inventoryRepository,
                           DietaryTagRepository dietaryTagRepository) {
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
        this.storeRepository = storeRepository;
        this.listingRepository = listingRepository;
        this.inventoryRepository = inventoryRepository;
        this.dietaryTagRepository = dietaryTagRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        System.out.println(">>> Starting Data Initialization...");

        // 1. Create User
        User user = new User();
        user.setRole("SUPPLIER");
        user.setEmail("contact@thelocalbakery.sg");
        user.setPasswordHash("encrypted_password_123");
        user.setDisplayName("Bakery Manager");
        user.setPhone("91234567");
        user.setStatus("ACTIVE");
        userRepository.save(user);

        // 2. Create Supplier Profile
        SupplierProfile supplier = new SupplierProfile();
        supplier.setUser(user);
        supplier.setBusinessName("The Local Bakery SG");
        supplier.setBusinessType("Bakery & Cafe");
        supplier.setVerificationStatus("VERIFIED");


        supplier.setRole("SUPPLIER");
        supplier.setStatus("ACTIVE");
        supplier.setEmail(user.getEmail());
        supplier.setPassword(user.getPasswordHash());


        supplier.setPhone("91234567");
        supplier.setDisplayName(user.getDisplayName());
        supplier.setCompanyName("The Local Bakery Pte Ltd");
        supplier.setUenNumber("202312345K");

        supplierRepository.save(supplier);

        // 3. Create Store
        Store store = new Store();
        store.setSupplierProfile(supplier);
        store.setStoreName("The Local Bakery - Clementi Mall");
        store.setAddressLine("3155 Commonwealth Ave W, #01-01 Clementi Mall");
        store.setPostalCode("129588");
        store.setLat(new BigDecimal("1.3150"));
        store.setLng(new BigDecimal("103.7650"));
        store.setActive(true);
        storeRepository.save(store);

        // --- Create Dietary Tag ---
        DietaryTag halalTag = new DietaryTag();
        halalTag.setTagName("Halal Certified");
        dietaryTagRepository.save(halalTag);

        // 4. Create Listing
        Listing listing = new Listing();
        listing.setStore(store);
        listing.setTitle("Assorted Croissants Surprise Box");
        listing.setDescription("A surprise box containing 4 fresh butter and almond croissants.");
        listing.setOriginalPrice(new BigDecimal("18.00"));
        listing.setRescuePrice(new BigDecimal("6.50"));
        listing.setPickupStart(LocalDateTime.now().plusHours(2));
        listing.setPickupEnd(LocalDateTime.now().plusHours(5));
        listing.setExpiryAt(LocalDateTime.now().plusDays(1));
        listing.setStatus("ACTIVE");

        List<DietaryTag> tags = new ArrayList<>();
        tags.add(halalTag);
        listing.setDietaryTags(tags);

        listingRepository.save(listing);

        // 5. Create Inventory
        Inventory inventory = new Inventory();
        inventory.setListing(listing);
        inventory.setQtyAvailable(8);
        inventory.setQtyReserved(0);
        inventoryRepository.save(inventory);

        System.out.println(">>> Database initialized successfully!");
    }
}