package com.frh.backend.repository;

import com.frh.backend.Model.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    // Find searches by consumer
    List<SearchLog> findByConsumerConsumerId(Long consumerId);

    // Find searches by query text
    List<SearchLog> findByQueryTextContaining(String queryText);

    // Find searches with clicks
    @Query("SELECT sl FROM SearchLog sl " +
           "WHERE sl.clickedListing IS NOT NULL " +
           "ORDER BY sl.createdAt DESC")
    List<SearchLog> findSearchesWithClicks();

    // Find searches without clicks
    @Query("SELECT sl FROM SearchLog sl " +
           "WHERE sl.clickedListing IS NULL " +
           "ORDER BY sl.createdAt DESC")
    List<SearchLog> findSearchesWithoutClicks();

    // Get popular search queries
    @Query("SELECT sl.queryText, COUNT(sl) as searchCount " +
           "FROM SearchLog sl " +
           "GROUP BY sl.queryText " +
           "ORDER BY searchCount DESC")
    List<Object[]> findPopularQueries();

    // Get searches with low results
    @Query("SELECT sl FROM SearchLog sl " +
           "WHERE sl.resultsCount < :maxResults " +
           "ORDER BY sl.createdAt DESC")
    List<SearchLog> findLowResultSearches(@Param("maxResults") Integer maxResults);

    // Get searches with no results
    @Query("SELECT sl FROM SearchLog sl " +
           "WHERE sl.resultsCount = 0 " +
           "ORDER BY sl.createdAt DESC")
    List<SearchLog> findZeroResultSearches();

    // Find recent searches by consumer
    @Query("SELECT sl FROM SearchLog sl " +
           "WHERE sl.consumer.consumerId = :consumerId " +
           "ORDER BY sl.createdAt DESC")
    List<SearchLog> findRecentByConsumer(@Param("consumerId") Long consumerId);

    // Find searches within date range
    @Query("SELECT sl FROM SearchLog sl " +
           "WHERE sl.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY sl.createdAt DESC")
    List<SearchLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Calculate average click position
    @Query("SELECT AVG(sl.clickPosition) FROM SearchLog sl " +
           "WHERE sl.clickPosition IS NOT NULL")
    Double calculateAvgClickPosition();

    // Get click-through rate for searches
    @Query("SELECT " +
           "COUNT(CASE WHEN sl.clickedListing IS NOT NULL THEN 1 END) * 1.0 / COUNT(sl) " +
           "FROM SearchLog sl")
    Double calculateSearchCTR();

    // Find searches by session
    List<SearchLog> findBySessionId(String sessionId);
}
