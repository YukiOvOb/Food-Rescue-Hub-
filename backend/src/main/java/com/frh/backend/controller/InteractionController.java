package com.frh.backend.controller;

import com.frh.backend.Model.UserInteraction;
import com.frh.backend.dto.UserInteractionRequest;
import com.frh.backend.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for recording user interactions
 * Tracks VIEW, CLICK, SEARCH, and ADD_TO_CART events
 */
@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InteractionController {

    private final InteractionService interactionService;

    /**
     * Record a single user interaction
     *
     * POST /api/interactions/record
     * Body: {
     *   "consumerId": 1,
     *   "listingId": 5,
     *   "interactionType": "CLICK",
     *   "sessionId": "abc123",
     *   "deviceType": "Android"
     * }
     */
    @PostMapping("/record")
    public ResponseEntity<Map<String, Object>> recordInteraction(
            @RequestBody UserInteractionRequest request
    ) {
        try {
            UserInteraction interaction = interactionService.recordInteraction(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interactionId", interaction.getInteractionId());
            response.put("message", "Interaction recorded successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Failed to record interaction");

            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Record multiple interactions in batch (for optimization)
     *
     * POST /api/interactions/batch
     * Body: [
     *   {"consumerId": 1, "listingId": 5, "interactionType": "VIEW"},
     *   {"consumerId": 1, "listingId": 6, "interactionType": "VIEW"}
     * ]
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> recordInteractionsBatch(
            @RequestBody List<UserInteractionRequest> requests
    ) {
        try {
            interactionService.recordInteractionsBatch(requests);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", requests.size());
            response.put("message", "Batch interactions recorded successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Failed to record batch interactions");

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "Interaction Tracking API");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
