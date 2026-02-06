package com.frh.backend.controller;

import com.frh.backend.Model.Order;
import com.frh.backend.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @MockitoBean
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

    /* -----------------------------
       GET ORDERS BY STORE – SUCCESS
       ----------------------------- */
    @Test
    void getOrdersByStore_success() throws Exception {

        Order order1 = new Order();
        order1.setOrderId(1L);
        Order order2 = new Order();
        order2.setOrderId(2L);

        Mockito.when(orderService.getOrdersByStore(1L))
                .thenReturn(List.of(order1, order2));

        mockMvc.perform(get("/api/orders/store/{storeId}", 1L))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       GET ORDERS BY STORE – ERROR
       ----------------------------- */
    @Test
    void getOrdersByStore_error() throws Exception {

        Mockito.when(orderService.getOrdersByStore(1L))
                .thenThrow(new RuntimeException("Store not found"));

        mockMvc.perform(get("/api/orders/store/{storeId}", 1L))
                .andExpect(status().isInternalServerError());
    }

    /* -----------------------------
       GET ORDERS BY STATUS – SUCCESS
       ----------------------------- */
    @Test
    void getOrdersByStatus_success() throws Exception {

        Order order1 = new Order();
        order1.setOrderId(1L);

        Mockito.when(orderService.getOrdersByStatus("COMPLETED"))
                .thenReturn(List.of(order1));

        mockMvc.perform(get("/api/orders/status/{status}", "COMPLETED"))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       GET ORDERS BY STATUS – ERROR
       ----------------------------- */
    @Test
    void getOrdersByStatus_error() throws Exception {

        Mockito.when(orderService.getOrdersByStatus("INVALID"))
                .thenThrow(new RuntimeException("Invalid status"));

        mockMvc.perform(get("/api/orders/status/{status}", "INVALID"))
                .andExpect(status().isInternalServerError());
    }

    /* -----------------------------
       GET ORDERS BY STORE AND STATUS – SUCCESS
       ----------------------------- */
    @Test
    void getOrdersByStoreAndStatus_success() throws Exception {

        Order order1 = new Order();
        order1.setOrderId(1L);

        Mockito.when(orderService.getOrdersByStoreAndStatus(1L, "COMPLETED"))
                .thenReturn(List.of(order1));

        mockMvc.perform(get("/api/orders/store/{storeId}/status/{status}", 1L, "COMPLETED"))
                .andExpect(status().isOk());
    }

    /* -----------------------------
       GET ORDERS BY STORE AND STATUS – ERROR
       ----------------------------- */
    @Test
    void getOrdersByStoreAndStatus_error() throws Exception {

        Mockito.when(orderService.getOrdersByStoreAndStatus(1L, "COMPLETED"))
                .thenThrow(new RuntimeException("Error retrieving orders"));

        mockMvc.perform(get("/api/orders/store/{storeId}/status/{status}", 1L, "COMPLETED"))
                .andExpect(status().isInternalServerError());
    }

    /* -----------------------------
       GET ALL ORDERS – ERROR
       ----------------------------- */
    @Test
    void getAllOrders_error() throws Exception {

        Mockito.when(orderService.getAllOrders())
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isInternalServerError());
    }

    /* -----------------------------
       UPDATE ORDER – ERROR
       ----------------------------- */
    @Test
    void updateOrder_error() throws Exception {

        Mockito.when(orderService.updateOrder(Mockito.eq(1L), Mockito.any()))
                .thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(put("/api/orders/{orderId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Order())))
                .andExpect(status().isBadRequest());
    }

    /* -----------------------------
       UPDATE ORDER STATUS – ERROR
       ----------------------------- */
    @Test
    void updateOrderStatus_error() throws Exception {

        Mockito.when(orderService.updateOrderStatus(1L, "INVALID"))
                .thenThrow(new RuntimeException("Invalid status"));

        mockMvc.perform(patch("/api/orders/{orderId}/status", 1L)
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    /* -----------------------------
       CANCEL ORDER – ERROR
       ----------------------------- */
    @Test
    void cancelOrder_error() throws Exception {

        Mockito.when(orderService.cancelOrder(Mockito.eq(1L), Mockito.any()))
                .thenThrow(new RuntimeException("Cannot cancel order"));

        mockMvc.perform(patch("/api/orders/{orderId}/cancel", 1L))
                .andExpect(status().isBadRequest());
    }

    /* -----------------------------
       DELETE ORDER – ERROR
       ----------------------------- */
    @Test
    void deleteOrder_error() throws Exception {

        Mockito.doThrow(new RuntimeException("Cannot delete order"))
                .when(orderService)
                .deleteOrder(1L);

        mockMvc.perform(delete("/api/orders/{orderId}", 1L))
                .andExpect(status().isBadRequest());
    }

    /* -----------------------------
       GET ORDER BY ID – ERROR
       ----------------------------- */
    @Test
    void getOrderById_error() throws Exception {

        Mockito.when(orderService.getOrderById(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/orders/{orderId}", 1L))
                .andExpect(status().isNotFound());
    }

    /* -----------------------------
       GET ORDERS BY CONSUMER – ERROR
       ----------------------------- */
    @Test
    void getOrdersByConsumer_error() throws Exception {

        Mockito.when(orderService.getOrdersByConsumer(1L))
                .thenThrow(new RuntimeException("Error retrieving orders"));

        mockMvc.perform(get("/api/orders/consumer")
                        .sessionAttr("USER_ID", 1L)
                        .sessionAttr("USER_ROLE", "CONSUMER"))
                .andExpect(status().isInternalServerError());
    }

    /* -----------------------------
       CREATE ORDER – NO SESSION ATTRIBUTES
       ----------------------------- */
    @Test
    void createOrder_noSessionAttributes() throws Exception {

        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    /* -----------------------------
       GET ORDERS BY CONSUMER – EMPTY LIST
       ----------------------------- */
    @Test
    void getOrdersByConsumer_emptyList() throws Exception {

        Mockito.when(orderService.getOrdersByConsumer(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/orders/consumer")
                        .sessionAttr("USER_ID", 1L)
                        .sessionAttr("USER_ROLE", "CONSUMER"))
                .andExpect(status().isOk());
    }
}
