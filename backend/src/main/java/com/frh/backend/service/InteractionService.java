package com.frh.backend.service;

import com.frh.backend.Model.*;
import com.frh.backend.dto.UserInteractionRequest;
import com.frh.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user interactions
 * Records VIEW, CLICK, SEARCH, and ADD_TO_CART events
 */
@Service
@RequiredArgsConstructor
public class InteractionService {

    private final UserInteractionRepository userInteractionRepository;
    private final ConsumerProfileRepository consumerProfileRepository;
    private final ListingRepository listingRepository;
    private final ListingStatsRepository listingStatsRepository;
    private final ConsumerStatsRepository consumerStatsRepository;

    /**
     * Record a user interaction (VIEW, CLICK, etc.)
     */
    @Transactional
    public UserInteraction recordInteraction(UserInteractionRequest request) {
        // Validate consumer exists
        ConsumerProfile consumer = consumerProfileRepository.findById(request.getConsumerId())
                .orElseThrow(() -> new RuntimeException("Consumer not found: " + request.getConsumerId()));

        // Validate listing exists
        Listing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new RuntimeException("Listing not found: " + request.getListingId()));

        // Create interaction record
        UserInteraction interaction = new UserInteraction();
        interaction.setConsumer(consumer);
        interaction.setListing(listing);
        interaction.setInteractionType(request.getInteractionType());
        interaction.setSessionId(request.getSessionId());
        interaction.setDeviceType(request.getDeviceType() != null ? request.getDeviceType() : "Android");

        // Save interaction
        UserInteraction savedInteraction = userInteractionRepository.save(interaction);

        // Update statistics asynchronously (in background)
        updateStatistics(request.getConsumerId(), request.getListingId(), request.getInteractionType());

        return savedInteraction;
    }

    /**
     * Update listing and consumer statistics based on interaction type
     */
    private void updateStatistics(Long consumerId, Long listingId, UserInteraction.InteractionType type) {
        try {
            // Update listing stats
            ListingStats listingStats = listingStatsRepository.findByListingId(listingId)
                    .orElseGet(() -> {
                        ListingStats newStats = new ListingStats();
                        Listing listing = listingRepository.findById(listingId).orElse(null);
                        newStats.setListing(listing);
                        return newStats;
                    });

            switch (type) {
                case VIEW:
                    listingStats.setViewCount(listingStats.getViewCount() + 1);
                    break;
                case CLICK:
                    listingStats.setClickCount(listingStats.getClickCount() + 1);
                    break;
                case ADD_TO_CART:
                    listingStats.setAddToCartCount(listingStats.getAddToCartCount() + 1);
                    break;
                default:
                    break;
            }

            listingStatsRepository.save(listingStats);

            // Update consumer stats
            ConsumerStats consumerStats = consumerStatsRepository.findByConsumerId(consumerId)
                    .orElseGet(() -> {
                        ConsumerStats newStats = new ConsumerStats();
                        ConsumerProfile consumer = consumerProfileRepository.findById(consumerId).orElse(null);
                        newStats.setConsumer(consumer);
                        return newStats;
                    });

            if (type == UserInteraction.InteractionType.VIEW) {
                consumerStats.setTotalViews(consumerStats.getTotalViews() + 1);
            } else if (type == UserInteraction.InteractionType.CLICK) {
                consumerStats.setTotalClicks(consumerStats.getTotalClicks() + 1);
            }

            consumerStatsRepository.save(consumerStats);

        } catch (Exception e) {
            // Log error but don't fail the main transaction
            System.err.println("[WARN] Failed to update statistics: " + e.getMessage());
        }
    }

    /**
     * Batch record multiple interactions (for optimization)
     */
    @Transactional
    public void recordInteractionsBatch(java.util.List<UserInteractionRequest> requests) {
        for (UserInteractionRequest request : requests) {
            try {
                recordInteraction(request);
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to record interaction: " + e.getMessage());
            }
        }
    }
}
