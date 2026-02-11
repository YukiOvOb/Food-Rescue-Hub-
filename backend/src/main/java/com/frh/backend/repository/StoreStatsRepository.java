package com.frh.backend.repository;

import com.frh.backend.model.StoreStats;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreStatsRepository extends JpaRepository<StoreStats, Long> {

  // Find stats by store ID
  Optional<StoreStats> findByStoreId(Long storeId);

  // Find top rated stores
  @Query(
      "SELECT ss FROM StoreStats ss "
          + "WHERE ss.totalRatings >= :minRatings "
          + "ORDER BY ss.avgRating DESC")
  List<StoreStats> findTopRated(@Param("minRatings") Integer minRatings);

  // Find stores with best completion rate
  @Query(
      "SELECT ss FROM StoreStats ss "
          + "WHERE ss.totalOrders >= :minOrders "
          + "ORDER BY ss.completionRate DESC")
  List<StoreStats> findBestCompletionRate(@Param("minOrders") Integer minOrders);

  // Find stores with best on-time rate
  @Query(
      "SELECT ss FROM StoreStats ss "
          + "WHERE ss.completedOrders >= :minOrders "
          + "ORDER BY ss.onTimeRate DESC")
  List<StoreStats> findBestOnTimeRate(@Param("minOrders") Integer minOrders);

  // Find most viewed stores
  @Query("SELECT ss FROM StoreStats ss " + "ORDER BY ss.totalViews DESC")
  List<StoreStats> findMostViewed();

  // Find stores with most active listings
  @Query(
      "SELECT ss FROM StoreStats ss "
          + "WHERE ss.activeListings > 0 "
          + "ORDER BY ss.activeListings DESC")
  List<StoreStats> findMostActiveListings();

  // Find stores with many orders
  @Query("SELECT ss FROM StoreStats ss " + "ORDER BY ss.completedOrders DESC")
  List<StoreStats> findMostOrders();

  // Find stores by rating range
  @Query(
      "SELECT ss FROM StoreStats ss "
          + "WHERE ss.avgRating BETWEEN :minRating AND :maxRating "
          + "ORDER BY ss.avgRating DESC")
  List<StoreStats> findByRatingRange(
      @Param("minRating") BigDecimal minRating, @Param("maxRating") BigDecimal maxRating);

  // Get aggregate store stats
  @Query(
      "SELECT AVG(ss.avgRating), SUM(ss.totalOrders), "
          + "SUM(ss.completedOrders), AVG(ss.completionRate) "
          + "FROM StoreStats ss")
  Object[] getAggregateStats();

  // Find underperforming stores (low completion rate)
  @Query(
      "SELECT ss FROM StoreStats ss "
          + "WHERE ss.totalOrders >= :minOrders "
          + "AND ss.completionRate < :maxRate "
          + "ORDER BY ss.completionRate ASC")
  List<StoreStats> findUnderperforming(
      @Param("minOrders") Integer minOrders, @Param("maxRate") Double maxRate);
}
