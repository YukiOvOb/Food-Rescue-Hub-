package com.frh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frh.backend.model.ConsumerProfile;
import com.frh.backend.model.ConsumerStats;
import com.frh.backend.model.Listing;
import com.frh.backend.model.ListingStats;
import com.frh.backend.model.UserInteraction;
import com.frh.backend.dto.UserInteractionRequest;
import com.frh.backend.repository.ConsumerProfileRepository;
import com.frh.backend.repository.ConsumerStatsRepository;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.ListingStatsRepository;
import com.frh.backend.repository.UserInteractionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

  @Mock private UserInteractionRepository userInteractionRepository;

  @Mock private ConsumerProfileRepository consumerProfileRepository;

  @Mock private ListingRepository listingRepository;

  @Mock private ListingStatsRepository listingStatsRepository;

  @Mock private ConsumerStatsRepository consumerStatsRepository;

  @InjectMocks private InteractionService interactionService;

  @Test
  void recordInteraction_view_createsStatsAndDefaultsDeviceType() {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(1L);
    Listing listing = new Listing();
    listing.setListingId(10L);

    UserInteractionRequest request = request(1L, 10L, UserInteraction.InteractionType.VIEW, null);

    when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
    when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
    when(userInteractionRepository.save(any(UserInteraction.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(listingStatsRepository.findByListingId(10L)).thenReturn(Optional.empty());
    when(consumerStatsRepository.findByConsumerId(1L)).thenReturn(Optional.empty());
    when(listingStatsRepository.save(any(ListingStats.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(consumerStatsRepository.save(any(ConsumerStats.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserInteraction result = interactionService.recordInteraction(request);

    assertEquals("Android", result.getDeviceType());
    assertEquals(UserInteraction.InteractionType.VIEW, result.getInteractionType());

    ArgumentCaptor<ListingStats> listingStatsCaptor = ArgumentCaptor.forClass(ListingStats.class);
    verify(listingStatsRepository).save(listingStatsCaptor.capture());
    assertEquals(1, listingStatsCaptor.getValue().getViewCount());
    assertEquals(0, listingStatsCaptor.getValue().getClickCount());

    ArgumentCaptor<ConsumerStats> consumerStatsCaptor =
        ArgumentCaptor.forClass(ConsumerStats.class);
    verify(consumerStatsRepository).save(consumerStatsCaptor.capture());
    assertEquals(1, consumerStatsCaptor.getValue().getTotalViews());
    assertEquals(0, consumerStatsCaptor.getValue().getTotalClicks());
  }

  @Test
  void recordInteraction_click_updatesExistingStats() {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(2L);
    Listing listing = new Listing();
    listing.setListingId(20L);

    ListingStats listingStats = new ListingStats();
    listingStats.setListing(listing);
    listingStats.setViewCount(10);
    listingStats.setClickCount(3);
    listingStats.setAddToCartCount(2);

    ConsumerStats consumerStats = new ConsumerStats();
    consumerStats.setConsumer(consumer);
    consumerStats.setTotalViews(7);
    consumerStats.setTotalClicks(5);

    UserInteractionRequest request = request(2L, 20L, UserInteraction.InteractionType.CLICK, "iOS");

    when(consumerProfileRepository.findById(2L)).thenReturn(Optional.of(consumer));
    when(listingRepository.findById(20L)).thenReturn(Optional.of(listing));
    when(userInteractionRepository.save(any(UserInteraction.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(listingStatsRepository.findByListingId(20L)).thenReturn(Optional.of(listingStats));
    when(consumerStatsRepository.findByConsumerId(2L)).thenReturn(Optional.of(consumerStats));
    when(listingStatsRepository.save(any(ListingStats.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(consumerStatsRepository.save(any(ConsumerStats.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserInteraction result = interactionService.recordInteraction(request);

    assertEquals("iOS", result.getDeviceType());
    assertEquals(4, listingStats.getClickCount());
    assertEquals(6, consumerStats.getTotalClicks());
    assertEquals(7, consumerStats.getTotalViews());
  }

  @Test
  void recordInteraction_addToCart_updatesOnlyAddToCartCounter() {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(3L);
    Listing listing = new Listing();
    listing.setListingId(30L);

    ListingStats listingStats = new ListingStats();
    listingStats.setListing(listing);
    listingStats.setViewCount(1);
    listingStats.setClickCount(1);
    listingStats.setAddToCartCount(0);

    ConsumerStats consumerStats = new ConsumerStats();
    consumerStats.setConsumer(consumer);
    consumerStats.setTotalViews(2);
    consumerStats.setTotalClicks(3);

    when(consumerProfileRepository.findById(3L)).thenReturn(Optional.of(consumer));
    when(listingRepository.findById(30L)).thenReturn(Optional.of(listing));
    when(userInteractionRepository.save(any(UserInteraction.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(listingStatsRepository.findByListingId(30L)).thenReturn(Optional.of(listingStats));
    when(consumerStatsRepository.findByConsumerId(3L)).thenReturn(Optional.of(consumerStats));
    when(listingStatsRepository.save(any(ListingStats.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(consumerStatsRepository.save(any(ConsumerStats.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    interactionService.recordInteraction(
        request(3L, 30L, UserInteraction.InteractionType.ADD_TO_CART, "Android"));

    assertEquals(1, listingStats.getAddToCartCount());
    assertEquals(2, consumerStats.getTotalViews());
    assertEquals(3, consumerStats.getTotalClicks());
  }

  @Test
  void recordInteraction_missingConsumer_throws() {
    when(consumerProfileRepository.findById(99L)).thenReturn(Optional.empty());

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () ->
                interactionService.recordInteraction(
                    request(99L, 1L, UserInteraction.InteractionType.VIEW, "Android")));

    assertEquals("Consumer not found: 99", ex.getMessage());
  }

  @Test
  void recordInteraction_missingListing_throws() {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(1L);
    when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
    when(listingRepository.findById(404L)).thenReturn(Optional.empty());

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () ->
                interactionService.recordInteraction(
                    request(1L, 404L, UserInteraction.InteractionType.VIEW, "Android")));

    assertEquals("Listing not found: 404", ex.getMessage());
  }

  @Test
  void recordInteractionsBatch_continuesAfterFailures() {
    UserInteractionRequest invalid =
        request(1L, 1L, UserInteraction.InteractionType.VIEW, "Android");
    UserInteractionRequest valid =
        request(2L, 2L, UserInteraction.InteractionType.CLICK, "Android");

    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(2L);
    Listing listing = new Listing();
    listing.setListingId(2L);
    ListingStats listingStats = new ListingStats();
    listingStats.setListing(listing);
    ConsumerStats consumerStats = new ConsumerStats();
    consumerStats.setConsumer(consumer);

    when(consumerProfileRepository.findById(1L)).thenReturn(Optional.empty());
    when(consumerProfileRepository.findById(2L)).thenReturn(Optional.of(consumer));
    when(listingRepository.findById(2L)).thenReturn(Optional.of(listing));
    when(userInteractionRepository.save(any(UserInteraction.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(listingStatsRepository.findByListingId(2L)).thenReturn(Optional.of(listingStats));
    when(consumerStatsRepository.findByConsumerId(2L)).thenReturn(Optional.of(consumerStats));
    when(listingStatsRepository.save(any(ListingStats.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(consumerStatsRepository.save(any(ConsumerStats.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    interactionService.recordInteractionsBatch(List.of(invalid, valid));

    verify(userInteractionRepository, times(1)).save(any(UserInteraction.class));
  }

  private static UserInteractionRequest request(
      Long consumerId, Long listingId, UserInteraction.InteractionType type, String deviceType) {
    UserInteractionRequest request = new UserInteractionRequest();
    request.setConsumerId(consumerId);
    request.setListingId(listingId);
    request.setInteractionType(type);
    request.setSessionId("session-1");
    request.setDeviceType(deviceType);
    return request;
  }
}

