package com.frh.backend.service;

import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.dto.ConsumerDTO;
import com.frh.backend.repository.ConsumerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {

    private final ConsumerProfileRepository consumerProfileRepository;

    /**
     * Get consumer profile by ID
     */
    public Optional<ConsumerDTO> getConsumerById(Long consumerId) {
        log.info("Fetching consumer profile for ID: {}", consumerId);

        Optional<ConsumerProfile> consumerOpt = consumerProfileRepository.findById(consumerId);

        return consumerOpt.map(this::convertToDTO);
    }

    /**
     * Get consumer profile by email
     */
    public Optional<ConsumerDTO> getConsumerByEmail(String email) {
        log.info("Fetching consumer profile for email: {}", email);

        Optional<ConsumerProfile> consumerOpt = consumerProfileRepository.findByEmail(email);

        return consumerOpt.map(this::convertToDTO);
    }

    /**
     * Convert ConsumerProfile entity to DTO
     */
    private ConsumerDTO convertToDTO(ConsumerProfile consumer) {
        ConsumerDTO dto = new ConsumerDTO();
        dto.setConsumerId(consumer.getConsumerId());
        dto.setEmail(consumer.getEmail());
        dto.setPhone(consumer.getPhone());
        dto.setDisplayName(consumer.getDisplayName());
        dto.setStatus(consumer.getStatus());
        dto.setRole(consumer.getRole());
        dto.setDefaultLat(consumer.getDefault_lat());
        dto.setDefaultLng(consumer.getDefault_lng());
        dto.setPreferences(consumer.getPreferences());

        return dto;
    }
}
