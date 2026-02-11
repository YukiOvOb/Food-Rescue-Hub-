package com.frh.backend.controller;

import com.frh.backend.dto.ConsumerDTO;
import com.frh.backend.service.ConsumerService;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consumer")
@RequiredArgsConstructor
@Slf4j
public class ConsumerController {

  private final ConsumerService consumerService;

  /** Get consumer profile by ID GET /api/consumer/{consumerId} */
  @GetMapping("/{consumerId}")
  public ResponseEntity<?> getConsumerById(@PathVariable Long consumerId) {
    log.info("GET /api/consumer/{}", consumerId);

    Optional<ConsumerDTO> consumerOpt = consumerService.getConsumerById(consumerId);

    if (consumerOpt.isPresent()) {
      return ResponseEntity.ok(consumerOpt.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  /** Get consumer profile by email GET /api/consumer/by-email?email=xxx */
  @GetMapping("/by-email")
  public ResponseEntity<?> getConsumerByEmail(@RequestParam String email) {
    log.info("GET /api/consumer/by-email?email={}", email);

    Optional<ConsumerDTO> consumerOpt = consumerService.getConsumerByEmail(email);

    if (consumerOpt.isPresent()) {
      return ResponseEntity.ok(consumerOpt.get());
    } else {
      return ResponseEntity.status(404)
          .body(Map.of("error", "Consumer not found with email: " + email));
    }
  }
}
