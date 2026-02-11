package com.frh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.Model.Listing;
import com.frh.backend.Model.ListingReview;
import com.frh.backend.Model.Order;
import com.frh.backend.Model.OrderItem;
import com.frh.backend.dto.CreateListingReviewRequest;
import com.frh.backend.dto.ListingReviewResponse;
import com.frh.backend.repository.ListingReviewRepository;
import com.frh.backend.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ListingReviewServiceTest {

  @Mock private ListingReviewRepository listingReviewRepository;

  @Mock private OrderRepository orderRepository;

  @InjectMocks private ListingReviewService listingReviewService;

  @Test
  void createReview_success() {
    Order order = order(1L, 10L, "COMPLETED", 100L);
    CreateListingReviewRequest request = request(1L, 100L, 5, " Excellent ");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(listingReviewRepository.existsByOrder_OrderIdAndListing_ListingIdAndConsumer_ConsumerId(
            1L, 100L, 10L))
        .thenReturn(false);
    when(listingReviewRepository.save(any(ListingReview.class)))
        .thenAnswer(
            invocation -> {
              ListingReview review = invocation.getArgument(0);
              review.setReviewId(88L);
              review.setCreatedAt(LocalDateTime.now());
              return review;
            });

    ListingReviewResponse created = listingReviewService.createReview(10L, request);

    assertEquals(88L, created.getReviewId());
    assertEquals(1L, created.getOrderId());
    assertEquals(100L, created.getListingId());
    assertEquals(5, created.getRating());
    assertEquals("Excellent", created.getComment());
    assertEquals(10L, created.getConsumerId());
    assertNotNull(created.getCreatedAt());
  }

  @Test
  void createReview_orderDoesNotBelongToUser_throwsForbidden() {
    Order order = order(1L, 99L, "COMPLETED", 100L);
    CreateListingReviewRequest request = request(1L, 100L, 5, "Good");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_orderNotFound_throwsNotFound() {
    CreateListingReviewRequest request = request(1L, 100L, 5, "Good");
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_orderConsumerNull_throwsForbidden() {
    Order order = order(1L, 10L, "COMPLETED", 100L);
    order.setConsumer(null);
    CreateListingReviewRequest request = request(1L, 100L, 5, "Good");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_nullStatus_throwsBadRequest() {
    Order order = order(1L, 10L, null, 100L);
    CreateListingReviewRequest request = request(1L, 100L, 5, "Good");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_notCompleted_throwsBadRequest() {
    Order order = order(1L, 10L, "PENDING", 100L);
    CreateListingReviewRequest request = request(1L, 100L, 5, "Good");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_orderItemsNull_throwsBadRequest() {
    Order order = order(1L, 10L, "COLLECTED", 100L);
    order.setOrderItems(null);
    CreateListingReviewRequest request = request(1L, 100L, 4, "Good");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_orderItemsEmpty_throwsBadRequest() {
    Order order = order(1L, 10L, "COLLECTED", 100L);
    order.setOrderItems(List.of());
    CreateListingReviewRequest request = request(1L, 100L, 4, "Good");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_orderContainsNullListing_throwsBadRequest() {
    Order order = order(1L, 10L, "COLLECTED", 100L);
    order.getOrderItems().get(0).setListing(null);
    CreateListingReviewRequest request = request(1L, 100L, 4, "Good");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_listingNotInOrder_throwsBadRequest() {
    Order order = order(1L, 10L, "COLLECTED", 100L);
    CreateListingReviewRequest request = request(1L, 101L, 4, "Good");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_duplicateReview_throwsConflict() {
    Order order = order(1L, 10L, "COMPLETED", 100L);
    CreateListingReviewRequest request = request(1L, 100L, 5, "Good");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(listingReviewRepository.existsByOrder_OrderIdAndListing_ListingIdAndConsumer_ConsumerId(
            1L, 100L, 10L))
        .thenReturn(true);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_ratingNull_throwsBadRequest() {
    Order order = order(1L, 10L, "COMPLETED", 100L);
    CreateListingReviewRequest request = request(1L, 100L, null, "Good");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(listingReviewRepository.existsByOrder_OrderIdAndListing_ListingIdAndConsumer_ConsumerId(
            1L, 100L, 10L))
        .thenReturn(false);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_ratingAboveFive_throwsBadRequest() {
    Order order = order(1L, 10L, "COMPLETED", 100L);
    CreateListingReviewRequest request = request(1L, 100L, 6, "Good");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(listingReviewRepository.existsByOrder_OrderIdAndListing_ListingIdAndConsumer_ConsumerId(
            1L, 100L, 10L))
        .thenReturn(false);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_commentNull_throwsBadRequest() {
    Order order = order(1L, 10L, "COMPLETED", 100L);
    CreateListingReviewRequest request = request(1L, 100L, 5, null);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(listingReviewRepository.existsByOrder_OrderIdAndListing_ListingIdAndConsumer_ConsumerId(
            1L, 100L, 10L))
        .thenReturn(false);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void createReview_commentBlank_throwsBadRequest() {
    Order order = order(1L, 10L, "COMPLETED", 100L);
    CreateListingReviewRequest request = request(1L, 100L, 5, "   ");

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(listingReviewRepository.existsByOrder_OrderIdAndListing_ListingIdAndConsumer_ConsumerId(
            1L, 100L, 10L))
        .thenReturn(false);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.createReview(10L, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    verify(listingReviewRepository, never()).save(any());
  }

  @Test
  void deleteReview_notOwner_throwsForbidden() {
    ListingReview review =
        review(8L, order(1L, 20L, "COMPLETED", 100L), consumer(20L), listing(100L));
    when(listingReviewRepository.findById(8L)).thenReturn(Optional.of(review));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.deleteReview(8L, 10L));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    verify(listingReviewRepository, never()).delete(any());
  }

  @Test
  void deleteReview_notFound_throwsNotFound() {
    when(listingReviewRepository.findById(8L)).thenReturn(Optional.empty());

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.deleteReview(8L, 10L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    verify(listingReviewRepository, never()).delete(any());
  }

  @Test
  void deleteReview_consumerNull_throwsForbidden() {
    ListingReview review =
        review(8L, order(1L, 20L, "COMPLETED", 100L), consumer(20L), listing(100L));
    review.setConsumer(null);
    when(listingReviewRepository.findById(8L)).thenReturn(Optional.of(review));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> listingReviewService.deleteReview(8L, 10L));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    verify(listingReviewRepository, never()).delete(any());
  }

  @Test
  void deleteReview_ownerCanDelete() {
    ListingReview review =
        review(8L, order(1L, 10L, "COMPLETED", 100L), consumer(10L), listing(100L));
    when(listingReviewRepository.findById(8L)).thenReturn(Optional.of(review));

    listingReviewService.deleteReview(8L, 10L);

    verify(listingReviewRepository).delete(review);
  }

  @Test
  void getReviewsByListing_mapsReviews() {
    ListingReview review =
        review(8L, order(1L, 10L, "COMPLETED", 100L), consumer(10L), listing(100L));
    when(listingReviewRepository.findByListing_ListingIdOrderByCreatedAtDesc(100L))
        .thenReturn(List.of(review));

    List<ListingReviewResponse> responses = listingReviewService.getReviewsByListing(100L);

    assertEquals(1, responses.size());
    assertEquals(8L, responses.get(0).getReviewId());
    assertEquals(100L, responses.get(0).getListingId());
    assertEquals(10L, responses.get(0).getConsumerId());
  }

  private CreateListingReviewRequest request(
      Long orderId, Long listingId, Integer rating, String comment) {
    CreateListingReviewRequest request = new CreateListingReviewRequest();
    request.setOrderId(orderId);
    request.setListingId(listingId);
    request.setRating(rating);
    request.setComment(comment);
    return request;
  }

  private Order order(Long orderId, Long consumerId, String status, Long listingId) {
    Order order = new Order();
    order.setOrderId(orderId);
    order.setConsumer(consumer(consumerId));
    order.setStatus(status);

    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setListing(listing(listingId));
    orderItem.setQuantity(1);
    orderItem.setUnitPrice(new BigDecimal("2.00"));
    orderItem.setLineTotal(new BigDecimal("2.00"));
    order.setOrderItems(List.of(orderItem));

    return order;
  }

  private ListingReview review(
      Long reviewId, Order order, ConsumerProfile consumer, Listing listing) {
    ListingReview review = new ListingReview();
    review.setReviewId(reviewId);
    review.setOrder(order);
    review.setConsumer(consumer);
    review.setListing(listing);
    review.setRating(5);
    review.setComment("Good");
    review.setCreatedAt(LocalDateTime.now());
    return review;
  }

  private ConsumerProfile consumer(Long id) {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(id);
    consumer.setDisplayName("User " + id);
    return consumer;
  }

  private Listing listing(Long id) {
    Listing listing = new Listing();
    listing.setListingId(id);
    listing.setTitle("Item " + id);
    return listing;
  }
}
