package com.frh.backend.repository;

import com.frh.backend.Model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    // Find all active listings
    List<Listing> findByStatus(String status);

    // Find listings by supplier (through store relationship)
    List<Listing> findByStore_SupplierProfile_SupplierId(Long supplierId);

    // Find active listings with available inventory
    @Query("SELECT l FROM Listing l " +
            "JOIN FETCH l.store s " +
            "JOIN FETCH l.inventory i " +
            "LEFT JOIN FETCH l.photos " +
            "LEFT JOIN FETCH l.listingFoodCategories lfc " +
            "LEFT JOIN FETCH lfc.category " +
            "LEFT JOIN FETCH s.supplierProfile sp " +
            "LEFT JOIN FETCH sp.storeType " +
            "WHERE l.status = 'ACTIVE' AND i.qtyAvailable > 0 " +
            "ORDER BY l.createdAt DESC")
    List<Listing> findAllActiveListingsWithDetails();

    // Find nearby listings based on coordinates and radius (in km)
    @Query("SELECT l FROM Listing l " +
            "JOIN FETCH l.store s " +
            "JOIN FETCH l.inventory i " +
            "LEFT JOIN FETCH l.photos " +
            "LEFT JOIN FETCH l.listingFoodCategories lfc " +
            "LEFT JOIN FETCH lfc.category " +
            "LEFT JOIN FETCH s.supplierProfile sp " +
            "LEFT JOIN FETCH sp.storeType " +
            "WHERE l.status = 'ACTIVE' AND i.qtyAvailable > 0 " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(s.lat)) * " +
            "cos(radians(s.lng) - radians(:lng)) + sin(radians(:lat)) * " +
            "sin(radians(s.lat)))) <= :radius " +
            "ORDER BY l.createdAt DESC")
    List<Listing> findNearbyListings(
        @Param("lat") Double lat,
        @Param("lng") Double lng,
        @Param("radius") Double radius
    );

    // Pessimistic lock for stock deduction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Listing l WHERE l.listingId = :id")
    Listing findByIdForUpdate(@Param("id") Long id);

    List<Listing> findByStoreStoreId(Long storeId); // this returns every listifng
}
