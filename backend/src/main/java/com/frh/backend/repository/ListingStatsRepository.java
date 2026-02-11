package com.frh.backend.repository;

import com.frh.backend.model.ListingStats;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingStatsRepository extends JpaRepository<ListingStats, Long> {

  // Find stats by listing ID
  Optional<ListingStats> findByListingId(Long listingId);

  // Find top viewed listings
  @Query("SELECT ls FROM ListingStats ls " + "ORDER BY ls.viewCount DESC")
  List<ListingStats> findTopViewed();

  // Find top clicked listings
  @Query("SELECT ls FROM ListingStats ls " + "ORDER BY ls.clickCount DESC")
  List<ListingStats> findTopClicked();

  // Find listings with best CTR
  @Query(
      "SELECT ls FROM ListingStats ls "
          + "WHERE ls.viewCount >= :minViews "
          + "ORDER BY ls.ctr DESC")
  List<ListingStats> findBestCtr(@Param("minViews") Integer minViews);

  // Find listings with best CVR
  @Query(
      "SELECT ls FROM ListingStats ls "
          + "WHERE ls.viewCount >= :minViews "
          + "ORDER BY ls.cvr DESC")
  List<ListingStats> findBestCvr(@Param("minViews") Integer minViews);

  // Find listings with orders
  @Query(
      "SELECT ls FROM ListingStats ls "
          + "WHERE ls.orderCount > 0 "
          + "ORDER BY ls.orderCount DESC")
  List<ListingStats> findWithOrders();

  // Get aggregate stats
  @Query(
      "SELECT SUM(ls.viewCount), SUM(ls.clickCount), "
          + "SUM(ls.addToCartCount), SUM(ls.orderCount) "
          + "FROM ListingStats ls")
  Object[] getAggregateStats();

  // Find listings by store
  @Query(
      "SELECT ls FROM ListingStats ls "
          + "WHERE ls.listing.store.storeId = :storeId "
          + "ORDER BY ls.viewCount DESC")
  List<ListingStats> findByStore(@Param("storeId") Long storeId);

  // Find underperforming listings (low CTR)
  @Query(
      "SELECT ls FROM ListingStats ls "
          + "WHERE ls.viewCount >= :minViews "
          + "AND ls.ctr < :maxCtr "
          + "ORDER BY ls.ctr ASC")
  List<ListingStats> findUnderperforming(
      @Param("minViews") Integer minViews, @Param("maxCtr") Double maxCtr);
}
