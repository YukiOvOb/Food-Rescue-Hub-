package com.frh.backend.controller;

import com.frh.backend.Model.Order;
import com.frh.backend.dto.CreateOrderRequest;
import com.frh.backend.dto.OrderSummaryDTO;
import com.frh.backend.dto.RejectOrderRequest;
import com.frh.backend.service.OrderService;
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
@WebMvcTest(SupplierOrderController.class)
class SupplierOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    /* --------------------------------
       CONSUMER – CREATE ORDER (SUCCESS)
       -------------------------------- */
    @Test
    void createOrder_success() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setListingId(1L);
        request.setConsumerId(10L);
        request.setQuantity(2);

        Order order = new Order();
        order.setOrderId(100L);

        Mockito.when(orderService.createOrder(Mockito.any()))
                .thenReturn(order);

        mockMvc.perform(post("/api/consumer/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(100L));
    }

    /* --------------------------------
       CONSUMER – CREATE ORDER (VALIDATION ERROR)
       -------------------------------- */
    @Test
    void createOrder_validationError() throws Exception {

        CreateOrderRequest invalidRequest = new CreateOrderRequest(); // missing required fields

        mockMvc.perform(post("/api/consumer/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /* --------------------------------
       SUPPLIER – GET ORDER QUEUE (NO STATUS)
       -------------------------------- */
    @Test
    void getOrderQueue_withoutStatus() throws Exception {

        OrderSummaryDTO dto = new OrderSummaryDTO();
        dto.setOrderId(1L);

        Mockito.when(orderService.getOrderQueue(5L, null))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/supplier/orders/{storeId}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    /* --------------------------------
       SUPPLIER – GET ORDER QUEUE (WITH STATUS)
       -------------------------------- */
    @Test
    void getOrderQueue_withStatus() throws Exception {

        Mockito.when(orderService.getOrderQueue(5L, "PENDING"))
                .thenReturn(List.of(new OrderSummaryDTO()));

        mockMvc.perform(get("/api/supplier/orders/{storeId}", 5L)
                        .param("status", "PENDING"))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       SUPPLIER – ACCEPT ORDER
       -------------------------------- */
    @Test
    void acceptOrder_success() throws Exception {

        Order order = new Order();
        order.setOrderId(20L);

        Mockito.when(orderService.acceptOrder(20L))
                .thenReturn(order);

        mockMvc.perform(put("/api/supplier/orders/{orderId}/accept", 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(20L));
    }

    /* --------------------------------
       SUPPLIER – REJECT ORDER
       -------------------------------- */
    @Test
    void rejectOrder_success() throws Exception {

        RejectOrderRequest request = new RejectOrderRequest();
        request.setReason("Out of stock");

        Mockito.when(orderService.rejectOrder(30L, "Out of stock"))
                .thenReturn(new Order());

        mockMvc.perform(put("/api/supplier/orders/{orderId}/reject", 30L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       SUPPLIER – CANCEL ACCEPTED ORDER
       -------------------------------- */
    @Test
    void cancelAcceptedOrder_success() throws Exception {

        RejectOrderRequest request = new RejectOrderRequest();
        request.setReason("Customer requested cancellation");

        Mockito.when(orderService.cancelAcceptedOrder(40L, "Customer requested cancellation"))
                .thenReturn(new Order());

        mockMvc.perform(put("/api/supplier/orders/{orderId}/cancel", 40L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
