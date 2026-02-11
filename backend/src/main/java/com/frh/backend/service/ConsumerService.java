package com.frh.backend.service;

import com.frh.backend.dto.ConsumerDto;
import com.frh.backend.model.ConsumerProfile;
import com.frh.backend.repository.ConsumerProfileRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {

  private final ConsumerProfileRepository consumerProfileRepository;

  /** Get consumer profile by ID */
  public Optional<ConsumerDto> getConsumerById(Long consumerId) {
    log.info("Fetching consumer profile for ID: {}", consumerId);

    Optional<ConsumerProfile> consumerOpt = consumerProfileRepository.findById(consumerId);

    return consumerOpt.map(this::convertToDto);
  }

  /** Get consumer profile by email */
  public Optional<ConsumerDto> getConsumerByEmail(String email) {
    log.info("Fetching consumer profile for email: {}", email);

    Optional<ConsumerProfile> consumerOpt = consumerProfileRepository.findByEmail(email);

    return consumerOpt.map(this::convertToDto);
  }

  /** Convert ConsumerProfile entity to DTO */
  private ConsumerDto convertToDto(ConsumerProfile consumer) {
    ConsumerDto dto = new ConsumerDto();
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
