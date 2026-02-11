package com.frh.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.dto.UpdateLocationRequest;
import com.frh.backend.repository.ConsumerProfileRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ConsumerProfileController.class)
class ConsumerProfileControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ConsumerProfileRepository consumerProfileRepository;

  @Autowired private ObjectMapper objectMapper;

  /* --------------------------------
  UPDATE LOCATION – SUCCESS
  -------------------------------- */
  @Test
  void updateLocation_success() throws Exception {

    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(1L);
    consumer.setDefault_lat(BigDecimal.valueOf(1.0));
    consumer.setDefault_lng(BigDecimal.valueOf(1.0));

    Mockito.when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));

    Mockito.when(consumerProfileRepository.save(Mockito.any())).thenReturn(consumer);

    UpdateLocationRequest request = new UpdateLocationRequest();
    request.setLatitude(BigDecimal.valueOf(1.3521));
    request.setLongitude(BigDecimal.valueOf(103.8198));

    mockMvc
        .perform(
            put("/api/consumer/profile/{consumerId}/location", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.latitude").value(1.3521))
        .andExpect(jsonPath("$.longitude").value(103.8198));
  }

  /* --------------------------------
  UPDATE LOCATION – NOT FOUND
  -------------------------------- */
  @Test
  void updateLocation_notFound() throws Exception {

    Mockito.when(consumerProfileRepository.findById(99L)).thenReturn(Optional.empty());

    UpdateLocationRequest request = new UpdateLocationRequest();
    request.setLatitude(BigDecimal.valueOf(1.0));
    request.setLongitude(BigDecimal.valueOf(1.0));

    mockMvc
        .perform(
            put("/api/consumer/profile/{consumerId}/location", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  /* --------------------------------
  GET LOCATION – SUCCESS
  -------------------------------- */
  @Test
  void getLocation_success() throws Exception {

    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(2L);
    consumer.setDefault_lat(BigDecimal.valueOf(1.3000));
    consumer.setDefault_lng(BigDecimal.valueOf(103.8000));

    Mockito.when(consumerProfileRepository.findById(2L)).thenReturn(Optional.of(consumer));

    mockMvc
        .perform(get("/api/consumer/profile/{consumerId}/location", 2L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.latitude").value(1.3000))
        .andExpect(jsonPath("$.longitude").value(103.8000));
  }

  /* --------------------------------
  GET LOCATION – NOT FOUND
  -------------------------------- */
  @Test
  void getLocation_notFound() throws Exception {

    Mockito.when(consumerProfileRepository.findById(88L)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/consumer/profile/{consumerId}/location", 88L))
        .andExpect(status().isNotFound());
  }
}
