package com.frh.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ListingReviewResponse {

    private Long reviewId;
    private Long orderId;
    private Long listingId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private Long consumerId;
    private String consumerDisplayName;
}
