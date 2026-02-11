package com.frh.backend.dto;

import com.frh.backend.model.UserInteraction;
import lombok.Data;

/** DTO for recording user interactions */
@Data
public class UserInteractionRequest {
  private Long consumerId;
  private Long listingId;
  private UserInteraction.InteractionType interactionType;
  private String sessionId;
  private String deviceType;
}
