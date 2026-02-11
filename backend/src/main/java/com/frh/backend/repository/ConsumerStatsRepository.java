package com.frh.backend.repository;

import com.frh.backend.model.ConsumerStats;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumerStatsRepository extends JpaRepository<ConsumerStats, Long> {

  // Find stats by consumer ID
  Optional<ConsumerStats> findByConsumerId(Long consumerId);

  // Find top spenders
  @Query("SELECT cs FROM ConsumerStats cs " + "ORDER BY cs.totalSpend DESC")
  List<ConsumerStats> findTopSpenders();

  // Find most active consumers (by orders)
  @Query("SELECT cs FROM ConsumerStats cs " + "ORDER BY cs.completedOrders DESC")
  List<ConsumerStats> findMostActive();

  // Find consumers with high engagement (views/clicks)
  @Query(
      "SELECT cs FROM ConsumerStats cs "
          + "WHERE cs.totalViews >= :minViews "
          + "ORDER BY cs.totalViews DESC")
  List<ConsumerStats> findHighEngagement(@Param("minViews") Integer minViews);

  // Find consumers by favorite store type
  List<ConsumerStats> findByFavoriteStoreType(String storeType);

  // Find consumers with high order value
  @Query(
      "SELECT cs FROM ConsumerStats cs "
          + "WHERE cs.avgOrderValue >= :minAvgValue "
          + "ORDER BY cs.avgOrderValue DESC")
  List<ConsumerStats> findHighValueConsumers(@Param("minAvgValue") BigDecimal minAvgValue);

  // Find consumers with cancellations
  @Query(
      "SELECT cs FROM ConsumerStats cs "
          + "WHERE cs.cancelledOrders > 0 "
          + "ORDER BY cs.cancelledOrders DESC")
  List<ConsumerStats> findWithCancellations();

  // Get aggregate consumer stats
  @Query(
      "SELECT SUM(cs.totalOrders), SUM(cs.completedOrders), "
          + "SUM(cs.totalSpend), AVG(cs.avgOrderValue) "
          + "FROM ConsumerStats cs")
  Object[] getAggregateStats();

  // Find new consumers (low order count)
  @Query(
      "SELECT cs FROM ConsumerStats cs "
          + "WHERE cs.totalOrders <= :maxOrders "
          + "ORDER BY cs.updatedAt DESC")
  List<ConsumerStats> findNewConsumers(@Param("maxOrders") Integer maxOrders);

  // Calculate total revenue from all consumers
  @Query("SELECT SUM(cs.totalSpend) FROM ConsumerStats cs")
  BigDecimal calculateTotalRevenue();
}
