package com.frh.backend.repository;

import com.frh.backend.Model.StoreRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface StoreRatingRepository extends JpaRepository<StoreRating, Long> {

    // Find ratings by store
    List<StoreRating> findByStoreStoreId(Long storeId);

    // Find ratings by consumer
    List<StoreRating> findByConsumerConsumerId(Long consumerId);

    // Find ratings by order
    List<StoreRating> findByOrderOrderId(Long orderId);

    // Find ratings above threshold
    @Query("SELECT sr FROM StoreRating sr " +
           "WHERE sr.rating >= :minRating " +
           "ORDER BY sr.rating DESC, sr.createdAt DESC")
    List<StoreRating> findByMinRating(@Param("minRating") BigDecimal minRating);

    // Calculate average rating for store
    @Query("SELECT AVG(sr.rating) FROM StoreRating sr " +
           "WHERE sr.store.storeId = :storeId")
    BigDecimal calculateAvgRating(@Param("storeId") Long storeId);

    // Count ratings for store
    @Query("SELECT COUNT(sr) FROM StoreRating sr " +
           "WHERE sr.store.storeId = :storeId")
    Long countByStore(@Param("storeId") Long storeId);

    // Get recent ratings for store
    @Query("SELECT sr FROM StoreRating sr " +
           "WHERE sr.store.storeId = :storeId " +
           "ORDER BY sr.createdAt DESC")
    List<StoreRating> findRecentByStore(@Param("storeId") Long storeId);

    // Find ratings with comments
    @Query("SELECT sr FROM StoreRating sr " +
           "WHERE sr.comment IS NOT NULL AND sr.comment != '' " +
           "ORDER BY sr.createdAt DESC")
    List<StoreRating> findAllWithComments();

    // Get top rated stores
    @Query("SELECT sr.store.storeId, AVG(sr.rating) as avgRating, COUNT(sr) as ratingCount " +
           "FROM StoreRating sr " +
           "GROUP BY sr.store.storeId " +
           "HAVING COUNT(sr) >= :minRatings " +
           "ORDER BY avgRating DESC")
    List<Object[]> findTopRatedStores(@Param("minRatings") Long minRatings);

    // Check if consumer has rated store
    @Query("SELECT COUNT(sr) > 0 FROM StoreRating sr " +
           "WHERE sr.consumer.consumerId = :consumerId " +
           "AND sr.store.storeId = :storeId")
    Boolean hasConsumerRatedStore(
        @Param("consumerId") Long consumerId,
        @Param("storeId") Long storeId
    );
}
