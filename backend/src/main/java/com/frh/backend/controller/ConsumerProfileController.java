package com.frh.backend.controller;

import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.dto.UpdateLocationRequest;
import com.frh.backend.repository.ConsumerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/consumer/profile")
@CrossOrigin(origins = "*")
public class ConsumerProfileController {

    @Autowired
    private ConsumerProfileRepository consumerProfileRepository;

    /**
     * Update consumer's default location
     * PUT /api/consumer/profile/{consumerId}/location
     */
    @PutMapping("/{consumerId}/location")
    public ResponseEntity<?> updateLocation(
            @PathVariable Long consumerId,
            @RequestBody UpdateLocationRequest request) {

        Optional<ConsumerProfile> optionalConsumer = consumerProfileRepository.findById(consumerId);

        if (optionalConsumer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ConsumerProfile consumer = optionalConsumer.get();
        consumer.setDefault_lat(request.getLatitude());
        consumer.setDefault_lng(request.getLongitude());

        consumerProfileRepository.save(consumer);

        // Return the updated location instead of plain text
        return ResponseEntity.ok(request);
    }

    /**
     * Get consumer's default location
     * GET /api/consumer/profile/{consumerId}/location
     */
    @GetMapping("/{consumerId}/location")
    public ResponseEntity<?> getLocation(@PathVariable Long consumerId) {

        Optional<ConsumerProfile> optionalConsumer = consumerProfileRepository.findById(consumerId);

        if (optionalConsumer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ConsumerProfile consumer = optionalConsumer.get();

        UpdateLocationRequest location = new UpdateLocationRequest();
        location.setLatitude(consumer.getDefault_lat());
        location.setLongitude(consumer.getDefault_lng());

        return ResponseEntity.ok(location);
    }
}
