package com.frh.backend.repository;

import com.frh.backend.model.UserStoreInteraction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStoreInteractionRepository
    extends JpaRepository<UserStoreInteraction, UserStoreInteraction.UserStoreInteractionId> {

  // Find interaction by consumer and store
  Optional<UserStoreInteraction> findByConsumerIdAndStoreId(Long consumerId, Long storeId);

  // Find all interactions by consumer
  List<UserStoreInteraction> findByConsumerId(Long consumerId);

  // Find all interactions by store
  List<UserStoreInteraction> findByStoreId(Long storeId);

  // Find consumer's top stores by order count
  @Query(
      "SELECT usi FROM UserStoreInteraction usi "
          + "WHERE usi.consumerId = :consumerId "
          + "ORDER BY usi.orderCount DESC")
  List<UserStoreInteraction> findTopStoresByOrders(@Param("consumerId") Long consumerId);

  // Find consumer's top stores by spending
  @Query(
      "SELECT usi FROM UserStoreInteraction usi "
          + "WHERE usi.consumerId = :consumerId "
          + "ORDER BY usi.totalSpend DESC")
  List<UserStoreInteraction> findTopStoresBySpend(@Param("consumerId") Long consumerId);

  // Find store's top consumers by spending
  @Query(
      "SELECT usi FROM UserStoreInteraction usi "
          + "WHERE usi.storeId = :storeId "
          + "ORDER BY usi.totalSpend DESC")
  List<UserStoreInteraction> findTopConsumersBySpend(@Param("storeId") Long storeId);

  // Find consumers who viewed but never ordered from store
  @Query(
      "SELECT usi FROM UserStoreInteraction usi "
          + "WHERE usi.storeId = :storeId "
          + "AND usi.viewCount > 0 "
          + "AND usi.orderCount = 0")
  List<UserStoreInteraction> findViewersWithoutOrders(@Param("storeId") Long storeId);

  // Count consumers for store
  @Query(
      "SELECT COUNT(usi) FROM UserStoreInteraction usi "
          + "WHERE usi.storeId = :storeId "
          + "AND usi.orderCount > 0")
  Long countCustomersWithOrders(@Param("storeId") Long storeId);

  // Find recent interactions by consumer
  @Query(
      "SELECT usi FROM UserStoreInteraction usi "
          + "WHERE usi.consumerId = :consumerId "
          + "AND usi.lastOrderAt IS NOT NULL "
          + "ORDER BY usi.lastOrderAt DESC")
  List<UserStoreInteraction> findRecentOrdersByConsumer(@Param("consumerId") Long consumerId);

  // Find high engagement interactions
  @Query(
      "SELECT usi FROM UserStoreInteraction usi "
          + "WHERE usi.viewCount >= :minViews "
          + "ORDER BY usi.viewCount DESC")
  List<UserStoreInteraction> findHighEngagement(@Param("minViews") Integer minViews);
}
