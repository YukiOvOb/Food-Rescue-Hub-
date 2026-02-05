package com.frh.backend.controller;

import com.frh.backend.Model.Order;
import com.frh.backend.service.ConsumerOrderService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ConsumerOrderController.class)
class ConsumerOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsumerOrderService consumerOrderService;

    /* --------------------------------
       GET ALL ORDERS BY CONSUMER
       -------------------------------- */
    @Test
    void getOrdersByConsumerId_success() throws Exception {

        Order order = Mockito.mock(Order.class);

        Mockito.when(consumerOrderService.getOrdersByConsumerId(1L))
                .thenReturn(List.of(order));

        mockMvc.perform(get("/api/consumer/orders/{consumerId}", 1L))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       GET ORDER BY ID – FOUND
       -------------------------------- */
    @Test
    void getOrderById_found() throws Exception {

        Order order = Mockito.mock(Order.class);

        Mockito.when(consumerOrderService.getOrderById(10L))
                .thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/consumer/orders/{consumerId}/order/{orderId}", 1L, 10L))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       GET ORDER BY ID – NOT FOUND
       -------------------------------- */
    @Test
    void getOrderById_notFound() throws Exception {

        Mockito.when(consumerOrderService.getOrderById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/consumer/orders/{consumerId}/order/{orderId}", 1L, 99L))
                .andExpect(status().isNotFound());
    }

    /* --------------------------------
       GET ORDERS BY STATUS
       -------------------------------- */
    @Test
    void getOrdersByConsumerIdAndStatus_success() throws Exception {

        Order order = Mockito.mock(Order.class);

        Mockito.when(consumerOrderService
                .getOrdersByConsumerIdAndStatus(1L, "COMPLETED"))
                .thenReturn(List.of(order));

        mockMvc.perform(get("/api/consumer/orders/{consumerId}/status/{status}", 1L, "COMPLETED"))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       UPDATE ORDER STATUS – SUCCESS
       -------------------------------- */
    @Test
    void updateOrderStatus_success() throws Exception {

        Order updatedOrder = Mockito.mock(Order.class);

        Mockito.when(consumerOrderService.updateOrderStatus(10L, "CANCELLED"))
                .thenReturn(updatedOrder);

        mockMvc.perform(patch("/api/consumer/orders/{consumerId}/order/{orderId}/status",
                        1L, 10L)
                        .param("status", "CANCELLED"))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       UPDATE ORDER STATUS – NOT FOUND
       -------------------------------- */
    @Test
    void updateOrderStatus_notFound() throws Exception {

        Mockito.when(consumerOrderService.updateOrderStatus(99L, "CANCELLED"))
                .thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(patch("/api/consumer/orders/{consumerId}/order/{orderId}/status",
                        1L, 99L)
                        .param("status", "CANCELLED"))
                .andExpect(status().isNotFound());
    }
}
