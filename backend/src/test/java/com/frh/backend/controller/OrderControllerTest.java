package com.frh.backend.controller;

import com.frh.backend.Model.Order;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    /* -----------------------------
       CREATE ORDER – UNAUTHORIZED
       ----------------------------- */
    @Test
    void createOrder_unauthorized() throws Exception {

        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    /* -----------------------------
       CREATE ORDER – SUCCESS
       ----------------------------- */
    @Test
    void createOrder_success() throws Exception {

        Order order = new Order();
        order.setOrderId(1L);
        order.setTotalAmount(BigDecimal.valueOf(25));

        Mockito.when(orderService.createOrderFromCart(
                Mockito.eq(1L),
                Mockito.any(),
                Mockito.any()))
                .thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .sessionAttr("USER_ID", 1L)
                        .sessionAttr("USER_ROLE", "CONSUMER")
                        .param("pickupSlotStart", LocalDateTime.now().plusHours(1).toString())
                        .param("pickupSlotEnd", LocalDateTime.now().plusHours(2).toString()))
                .andExpect(status().isCreated());
    }

    /* -----------------------------
       CREATE ORDER – FAILURE
       ----------------------------- */
    @Test
    void createOrder_failure() throws Exception {

        Mockito.when(orderService.createOrderFromCart(
                Mockito.eq(1L),
                Mockito.any(),
                Mockito.any()))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(post("/api/orders")
                        .sessionAttr("USER_ID", 1L)
                        .sessionAttr("USER_ROLE", "CONSUMER"))
                .andExpect(status().isBadRequest());
    }

    /* -----------------------------
       GET ORDER BY ID – FOUND
       ----------------------------- */
    @Test
    void getOrderById_success() throws Exception {

        Mockito.when(orderService.getOrderById(1L))
                .thenReturn(Optional.of(new Order()));

        mockMvc.perform(get("/api/orders/{orderId}", 1L))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       GET ORDER BY ID – NOT FOUND
       ----------------------------- */
    @Test
    void getOrderById_notFound() throws Exception {

        Mockito.when(orderService.getOrderById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/{orderId}", 99L))
                .andExpect(status().isNotFound());
    }

    /* -----------------------------
       GET ALL ORDERS
       ----------------------------- */
    @Test
    void getAllOrders_success() throws Exception {

        Mockito.when(orderService.getAllOrders())
                .thenReturn(List.of(new Order()));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       GET ORDERS BY CONSUMER – WRONG ROLE
       ----------------------------- */
    @Test
    void getOrdersByConsumer_wrongRole() throws Exception {

        mockMvc.perform(get("/api/orders/consumer")
                        .sessionAttr("USER_ROLE", "SUPPLIER"))
                .andExpect(status().isBadRequest());
    }

    /* -----------------------------
       GET ORDERS BY CONSUMER – SUCCESS
       ----------------------------- */
    @Test
    void getOrdersByConsumer_success() throws Exception {

        Mockito.when(orderService.getOrdersByConsumer(1L))
                .thenReturn(List.of(new Order()));

        mockMvc.perform(get("/api/orders/consumer")
                        .sessionAttr("USER_ID", 1L)
                        .sessionAttr("USER_ROLE", "CONSUMER"))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       UPDATE ORDER
       ----------------------------- */
    @Test
    void updateOrder_success() throws Exception {

        Mockito.when(orderService.updateOrder(Mockito.eq(1L), Mockito.any()))
                .thenReturn(new Order());

        mockMvc.perform(put("/api/orders/{orderId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Order())))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       UPDATE ORDER STATUS
       ----------------------------- */
    @Test
    void updateOrderStatus_success() throws Exception {

        Mockito.when(orderService.updateOrderStatus(1L, "COMPLETED"))
                .thenReturn(new Order());

        mockMvc.perform(patch("/api/orders/{orderId}/status", 1L)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       CANCEL ORDER
       ----------------------------- */
    @Test
    void cancelOrder_success() throws Exception {

        Mockito.when(orderService.cancelOrder(Mockito.eq(1L), Mockito.any()))
                .thenReturn(new Order());

        mockMvc.perform(patch("/api/orders/{orderId}/cancel", 1L))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       DELETE ORDER
       ----------------------------- */
    @Test
    void deleteOrder_success() throws Exception {

        Mockito.doNothing()
                .when(orderService)
                .deleteOrder(1L);

        mockMvc.perform(delete("/api/orders/{orderId}", 1L))
                .andExpect(status().isOk());
    }
}
