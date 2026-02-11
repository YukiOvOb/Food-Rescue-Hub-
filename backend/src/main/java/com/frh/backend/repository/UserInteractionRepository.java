package com.frh.backend.repository;

import com.frh.backend.model.UserInteraction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

  // Find interactions by consumer
  List<UserInteraction> findByConsumerConsumerId(Long consumerId);

  // Find interactions by listing
  List<UserInteraction> findByListingListingId(Long listingId);

  // Find interactions by type
  List<UserInteraction> findByInteractionType(UserInteraction.InteractionType interactionType);

  // Find interactions by consumer and type
  List<UserInteraction> findByConsumerConsumerIdAndInteractionType(
      Long consumerId, UserInteraction.InteractionType interactionType);

  // Find recent interactions by consumer
  @Query(
      "SELECT ui FROM UserInteraction ui "
          + "WHERE ui.consumer.consumerId = :consumerId "
          + "ORDER BY ui.createdAt DESC")
  List<UserInteraction> findRecentByConsumer(@Param("consumerId") Long consumerId);

  // Count interactions by listing and type
  @Query(
      "SELECT COUNT(ui) FROM UserInteraction ui "
          + "WHERE ui.listing.listingId = :listingId "
          + "AND ui.interactionType = :type")
  Long countByListingAndType(
      @Param("listingId") Long listingId, @Param("type") UserInteraction.InteractionType type);

  // Find interactions within time range
  @Query(
      "SELECT ui FROM UserInteraction ui "
          + "WHERE ui.createdAt BETWEEN :startDate AND :endDate "
          + "ORDER BY ui.createdAt DESC")
  List<UserInteraction> findByDateRange(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  // Get top viewed listings
  @Query(
      "SELECT ui.listing.listingId, COUNT(ui) as viewCount "
          + "FROM UserInteraction ui "
          + "WHERE ui.interactionType = 'VIEW' "
          + "GROUP BY ui.listing.listingId "
          + "ORDER BY viewCount DESC")
  List<Object[]> findTopViewedListings();

  // Get consumer interaction summary
  @Query(
      "SELECT ui.interactionType, COUNT(ui) "
          + "FROM UserInteraction ui "
          + "WHERE ui.consumer.consumerId = :consumerId "
          + "GROUP BY ui.interactionType")
  List<Object[]> getConsumerInteractionSummary(@Param("consumerId") Long consumerId);
}
