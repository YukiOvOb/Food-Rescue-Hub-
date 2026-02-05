package com.frh.backend.controller;

import com.frh.backend.Model.UserInteraction;
import com.frh.backend.dto.UserInteractionRequest;
import com.frh.backend.service.InteractionService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(InteractionController.class)
class InteractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InteractionService interactionService;

    @Autowired
    private ObjectMapper objectMapper;

    /* --------------------------------
       RECORD SINGLE INTERACTION – SUCCESS
       -------------------------------- */
    @Test
    void recordInteraction_success() throws Exception {

        UserInteraction interaction = new UserInteraction();
        interaction.setInteractionId(1L);

        Mockito.when(interactionService.recordInteraction(Mockito.any()))
                .thenReturn(interaction);

        UserInteractionRequest request = new UserInteractionRequest();
        request.setConsumerId(1L);
        request.setListingId(5L);
        request.setInteractionType(UserInteraction.InteractionType.CLICK);

        mockMvc.perform(post("/api/interactions/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.interactionId").value(1L))
                .andExpect(jsonPath("$.message")
                        .value("Interaction recorded successfully"));
    }

    /* --------------------------------
       RECORD SINGLE INTERACTION – FAILURE
       -------------------------------- */
    @Test
    void recordInteraction_failure() throws Exception {

        Mockito.when(interactionService.recordInteraction(Mockito.any()))
                .thenThrow(new RuntimeException("DB error"));

        UserInteractionRequest request = new UserInteractionRequest();
        request.setConsumerId(1L);
        request.setListingId(5L);
        request.setInteractionType(UserInteraction.InteractionType.CLICK);

        mockMvc.perform(post("/api/interactions/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Failed to record interaction"));
    }

    /* --------------------------------
       RECORD BATCH – SUCCESS
       -------------------------------- */
    @Test
    void recordInteractionsBatch_success() throws Exception {

        UserInteractionRequest req1 = new UserInteractionRequest();
        req1.setConsumerId(1L);
        req1.setListingId(5L);
        req1.setInteractionType(UserInteraction.InteractionType.VIEW);

        UserInteractionRequest req2 = new UserInteractionRequest();
        req2.setConsumerId(1L);
        req2.setListingId(6L);
        req2.setInteractionType(UserInteraction.InteractionType.VIEW);

        Mockito.doNothing()
                .when(interactionService)
                .recordInteractionsBatch(Mockito.anyList());

        mockMvc.perform(post("/api/interactions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(req1, req2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.message")
                        .value("Batch interactions recorded successfully"));
    }

    /* --------------------------------
       RECORD BATCH – FAILURE
       -------------------------------- */
    @Test
    void recordInteractionsBatch_failure() throws Exception {

        Mockito.doThrow(new RuntimeException("Batch error"))
                .when(interactionService)
                .recordInteractionsBatch(Mockito.anyList());

        UserInteractionRequest request = new UserInteractionRequest();
        request.setConsumerId(1L);
        request.setListingId(5L);
        request.setInteractionType(UserInteraction.InteractionType.VIEW);

        mockMvc.perform(post("/api/interactions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Failed to record batch interactions"));
    }

    /* --------------------------------
       HEALTH CHECK
       -------------------------------- */
    @Test
    void healthCheck_success() throws Exception {

        mockMvc.perform(get("/api/interactions/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.service")
                        .value("Interaction Tracking API"));
    }
}
