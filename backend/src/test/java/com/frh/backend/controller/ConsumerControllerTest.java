package com.frh.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.frh.backend.dto.ConsumerDTO;
import com.frh.backend.service.ConsumerService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ConsumerController.class)
class ConsumerControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ConsumerService consumerService;

  /* --------------------------------
  GET BY ID – FOUND
  -------------------------------- */
  @Test
  void getConsumerById_success() throws Exception {

    ConsumerDTO consumer = new ConsumerDTO();
    consumer.setConsumerId(1L);
    consumer.setEmail("user@test.com");

    Mockito.when(consumerService.getConsumerById(1L)).thenReturn(Optional.of(consumer));

    mockMvc
        .perform(get("/api/consumer/{consumerId}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.consumerId").value(1L))
        .andExpect(jsonPath("$.email").value("user@test.com"));
  }

  /* --------------------------------
  GET BY ID – NOT FOUND
  -------------------------------- */
  @Test
  void getConsumerById_notFound() throws Exception {

    Mockito.when(consumerService.getConsumerById(99L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/consumer/{consumerId}", 99L)).andExpect(status().isNotFound());
  }

  /* --------------------------------
  GET BY EMAIL – FOUND
  -------------------------------- */
  @Test
  void getConsumerByEmail_success() throws Exception {

    ConsumerDTO consumer = new ConsumerDTO();
    consumer.setConsumerId(2L);
    consumer.setEmail("found@test.com");

    Mockito.when(consumerService.getConsumerByEmail("found@test.com"))
        .thenReturn(Optional.of(consumer));

    mockMvc
        .perform(get("/api/consumer/by-email").param("email", "found@test.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.consumerId").value(2L))
        .andExpect(jsonPath("$.email").value("found@test.com"));
  }

  /* --------------------------------
  GET BY EMAIL – NOT FOUND
  -------------------------------- */
  @Test
  void getConsumerByEmail_notFound() throws Exception {

    Mockito.when(consumerService.getConsumerByEmail("missing@test.com"))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/consumer/by-email").param("email", "missing@test.com"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Consumer not found with email: missing@test.com"));
  }
}
