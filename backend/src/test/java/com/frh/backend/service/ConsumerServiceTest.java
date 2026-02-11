package com.frh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.frh.backend.model.ConsumerProfile;
import com.frh.backend.dto.ConsumerDTO;
import com.frh.backend.repository.ConsumerProfileRepository;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

  @Mock private ConsumerProfileRepository consumerProfileRepository;

  @InjectMocks private ConsumerService consumerService;

  @Test
  void getConsumerById_mapsToDtoWhenPresent() {
    ConsumerProfile profile = new ConsumerProfile();
    profile.setConsumerId(10L);
    profile.setEmail("user@test.com");
    profile.setPhone("90000001");
    profile.setDisplayName("User One");
    profile.setStatus("ACTIVE");
    profile.setRole("CONSUMER");
    profile.setDefault_lat(new BigDecimal("1.3000000"));
    profile.setDefault_lng(new BigDecimal("103.8000000"));
    profile.setPreferences(Map.of("diet", "vegan"));

    when(consumerProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

    Optional<ConsumerDTO> result = consumerService.getConsumerById(10L);

    assertTrue(result.isPresent());
    ConsumerDTO dto = result.orElseThrow();
    assertEquals(10L, dto.getConsumerId());
    assertEquals("user@test.com", dto.getEmail());
    assertEquals("90000001", dto.getPhone());
    assertEquals("User One", dto.getDisplayName());
    assertEquals("ACTIVE", dto.getStatus());
    assertEquals("CONSUMER", dto.getRole());
    assertEquals(new BigDecimal("1.3000000"), dto.getDefaultLat());
    assertEquals(new BigDecimal("103.8000000"), dto.getDefaultLng());
    assertEquals("vegan", dto.getPreferences().get("diet"));
  }

  @Test
  void getConsumerById_returnsEmptyWhenMissing() {
    when(consumerProfileRepository.findById(99L)).thenReturn(Optional.empty());

    Optional<ConsumerDTO> result = consumerService.getConsumerById(99L);

    assertTrue(result.isEmpty());
  }

  @Test
  void getConsumerByEmail_mapsToDtoWhenPresent() {
    ConsumerProfile profile = new ConsumerProfile();
    profile.setConsumerId(20L);
    profile.setEmail("email@test.com");
    profile.setDisplayName("Email User");
    profile.setStatus("ACTIVE");
    profile.setRole("CONSUMER");

    when(consumerProfileRepository.findByEmail("email@test.com")).thenReturn(Optional.of(profile));

    Optional<ConsumerDTO> result = consumerService.getConsumerByEmail("email@test.com");

    assertTrue(result.isPresent());
    assertEquals(20L, result.orElseThrow().getConsumerId());
    assertEquals("email@test.com", result.orElseThrow().getEmail());
  }

  @Test
  void getConsumerByEmail_returnsEmptyWhenMissing() {
    when(consumerProfileRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

    Optional<ConsumerDTO> result = consumerService.getConsumerByEmail("missing@test.com");

    assertTrue(result.isEmpty());
  }
}

