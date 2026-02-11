package com.frh.backend.service;

import com.frh.backend.dto.CreateListingReviewRequest;
import com.frh.backend.dto.ListingReviewResponse;
import com.frh.backend.model.Listing;
import com.frh.backend.model.ListingReview;
import com.frh.backend.model.Order;
import com.frh.backend.repository.ListingReviewRepository;
import com.frh.backend.repository.OrderRepository;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ListingReviewService {

  private static final Set<String> REVIEWABLE_STATUSES = Set.of("COMPLETED", "COLLECTED");

  private final ListingReviewRepository listingReviewRepository;
  private final OrderRepository orderRepository;

  @Transactional
  public ListingReviewResponse createReview(Long consumerId, CreateListingReviewRequest request) {
    Order order =
        orderRepository
            .findById(request.getOrderId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

    if (order.getConsumer() == null || !consumerId.equals(order.getConsumer().getConsumerId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Order does not belong to current user");
    }

    String status = order.getStatus() == null ? "" : order.getStatus().toUpperCase(Locale.ROOT);
    if (!REVIEWABLE_STATUSES.contains(status)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Order must be completed before submitting a review");
    }

    if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Order does not contain any listings");
    }

    Listing listing =
        order.getOrderItems().stream()
            .map(item -> item.getListing())
            .filter(
                itemListing ->
                    itemListing != null
                        && request.getListingId().equals(itemListing.getListingId()))
            .findFirst()
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Listing is not part of the specified order"));

    if (listingReviewRepository.existsByOrder_OrderIdAndListing_ListingIdAndConsumer_ConsumerId(
        request.getOrderId(), request.getListingId(), consumerId)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "You have already reviewed this listing for the order");
    }

    Integer rating = request.getRating();
    if (rating == null || rating < 1 || rating > 5) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
    }

    String comment = request.getComment() == null ? "" : request.getComment().trim();
    if (comment.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment is required");
    }

    ListingReview review = new ListingReview();
    review.setOrder(order);
    review.setListing(listing);
    review.setConsumer(order.getConsumer());
    review.setRating(rating);
    review.setComment(comment);

    ListingReview saved = listingReviewRepository.save(review);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<ListingReviewResponse> getReviewsByListing(Long listingId) {
    return listingReviewRepository.findByListing_ListingIdOrderByCreatedAtDesc(listingId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public void deleteReview(Long reviewId, Long consumerId) {
    ListingReview review =
        listingReviewRepository
            .findById(reviewId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

    if (review.getConsumer() == null || !consumerId.equals(review.getConsumer().getConsumerId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "You can only delete your own review");
    }

    listingReviewRepository.delete(review);
  }

  private ListingReviewResponse toResponse(ListingReview review) {
    ListingReviewResponse response = new ListingReviewResponse();
    response.setReviewId(review.getReviewId());
    response.setOrderId(review.getOrder().getOrderId());
    response.setListingId(review.getListing().getListingId());
    response.setRating(review.getRating());
    response.setComment(review.getComment());
    response.setCreatedAt(review.getCreatedAt());
    response.setConsumerId(review.getConsumer().getConsumerId());
    response.setConsumerDisplayName(review.getConsumer().getDisplayName());
    return response;
  }
}
