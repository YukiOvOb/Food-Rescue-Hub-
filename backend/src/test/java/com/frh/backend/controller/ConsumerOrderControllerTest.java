package com.frh.backend.controller;

import com.frh.backend.Model.Order;
import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.service.ConsumerOrderService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @MockitoBean
    private ConsumerOrderService consumerOrderService;

    /* --------------------------------
       GET ALL ORDERS BY CONSUMER
       -------------------------------- */
    @Test
    void getOrdersByConsumerId_success() throws Exception {

        Order order = Mockito.mock(Order.class);

        Mockito.when(consumerOrderService.getOrdersByConsumerId(1L))
                .thenReturn(List.of(order));

        mockMvc.perform(get("/api/consumer/orders")
                        .sessionAttr("USER_ID", 1L))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       GET ALL ORDERS BY CONSUMER - UNAUTHORIZED
       -------------------------------- */
    @Test
    void getOrdersByConsumerId_unauthorized() throws Exception {

        mockMvc.perform(get("/api/consumer/orders"))
                .andExpect(status().isUnauthorized());
    }

    /* --------------------------------
       GET ORDER BY ID – FOUND
       -------------------------------- */
    @Test
    void getOrderById_found() throws Exception {

        Order order = Mockito.mock(Order.class);
        ConsumerProfile consumer = Mockito.mock(ConsumerProfile.class);
        Mockito.when(order.getConsumer()).thenReturn(consumer);
        Mockito.when(consumer.getConsumerId()).thenReturn(1L);

        Mockito.when(consumerOrderService.getOrderById(10L))
                .thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/consumer/orders/order/{orderId}", 10L)
                        .sessionAttr("USER_ID", 1L))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       GET ORDER BY ID - UNAUTHORIZED
       -------------------------------- */
    @Test
    void getOrderById_unauthorized() throws Exception {

        mockMvc.perform(get("/api/consumer/orders/order/{orderId}", 10L))
                .andExpect(status().isUnauthorized());
    }

    /* --------------------------------
       GET ORDER BY ID – NOT FOUND
       -------------------------------- */
    @Test
    void getOrderById_notFound() throws Exception {

        Mockito.when(consumerOrderService.getOrderById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/consumer/orders/order/{orderId}", 99L)
                        .sessionAttr("USER_ID", 1L))
                .andExpect(status().isNotFound());
    }

    /* --------------------------------
       GET ORDER BY ID - FORBIDDEN (NO CONSUMER ON ORDER)
       -------------------------------- */
    @Test
    void getOrderById_forbiddenWhenOrderHasNoConsumer() throws Exception {

        Order order = Mockito.mock(Order.class);
        Mockito.when(order.getConsumer()).thenReturn(null);

        Mockito.when(consumerOrderService.getOrderById(10L))
                .thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/consumer/orders/order/{orderId}", 10L)
                        .sessionAttr("USER_ID", 1L))
                .andExpect(status().isForbidden());
    }

    /* --------------------------------
       GET ORDER BY ID - FORBIDDEN (DIFFERENT CONSUMER)
       -------------------------------- */
    @Test
    void getOrderById_forbiddenWhenDifferentOwner() throws Exception {

        Order order = Mockito.mock(Order.class);
        ConsumerProfile consumer = Mockito.mock(ConsumerProfile.class);
        Mockito.when(order.getConsumer()).thenReturn(consumer);
        Mockito.when(consumer.getConsumerId()).thenReturn(2L);

        Mockito.when(consumerOrderService.getOrderById(10L))
                .thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/consumer/orders/order/{orderId}", 10L)
                        .sessionAttr("USER_ID", 1L))
                .andExpect(status().isForbidden());
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

        mockMvc.perform(get("/api/consumer/orders/status/{status}", "COMPLETED")
                        .sessionAttr("USER_ID", 1L))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       GET ORDERS BY STATUS - UNAUTHORIZED
       -------------------------------- */
    @Test
    void getOrdersByConsumerIdAndStatus_unauthorized() throws Exception {

        mockMvc.perform(get("/api/consumer/orders/status/{status}", "COMPLETED"))
                .andExpect(status().isUnauthorized());
    }

    /* --------------------------------
       UPDATE ORDER STATUS – SUCCESS
       -------------------------------- */
    @Test
    void updateOrderStatus_success() throws Exception {

        Order existingOrder = Mockito.mock(Order.class);
        ConsumerProfile consumer = Mockito.mock(ConsumerProfile.class);
        Order updatedOrder = Mockito.mock(Order.class);
        Mockito.when(existingOrder.getConsumer()).thenReturn(consumer);
        Mockito.when(consumer.getConsumerId()).thenReturn(1L);

        Mockito.when(consumerOrderService.getOrderById(10L))
                .thenReturn(Optional.of(existingOrder));
        Mockito.when(consumerOrderService.updateOrderStatus(10L, "CANCELLED"))
                .thenReturn(updatedOrder);

        mockMvc.perform(patch("/api/consumer/orders/order/{orderId}/status", 10L)
                        .sessionAttr("USER_ID", 1L)
                        .param("status", "CANCELLED"))
                .andExpect(status().isOk());
    }

    /* --------------------------------
       UPDATE ORDER STATUS - UNAUTHORIZED
       -------------------------------- */
    @Test
    void updateOrderStatus_unauthorized() throws Exception {

        mockMvc.perform(patch("/api/consumer/orders/order/{orderId}/status", 10L)
                        .param("status", "CANCELLED"))
                .andExpect(status().isUnauthorized());
    }

    /* --------------------------------
       UPDATE ORDER STATUS – NOT FOUND
       -------------------------------- */
    @Test
    void updateOrderStatus_notFound() throws Exception {

        Mockito.when(consumerOrderService.getOrderById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/consumer/orders/order/{orderId}/status", 99L)
                        .sessionAttr("USER_ID", 1L)
                        .param("status", "CANCELLED"))
                .andExpect(status().isNotFound());
    }

    /* --------------------------------
       UPDATE ORDER STATUS - FORBIDDEN (CONSUMER ID MISSING)
       -------------------------------- */
    @Test
    void updateOrderStatus_forbiddenWhenOrderConsumerIdMissing() throws Exception {

        Order existingOrder = Mockito.mock(Order.class);
        ConsumerProfile consumer = Mockito.mock(ConsumerProfile.class);
        Mockito.when(existingOrder.getConsumer()).thenReturn(consumer);
        Mockito.when(consumer.getConsumerId()).thenReturn(null);

        Mockito.when(consumerOrderService.getOrderById(10L))
                .thenReturn(Optional.of(existingOrder));

        mockMvc.perform(patch("/api/consumer/orders/order/{orderId}/status", 10L)
                        .sessionAttr("USER_ID", 1L)
                        .param("status", "CANCELLED"))
                .andExpect(status().isForbidden());

        Mockito.verify(consumerOrderService, Mockito.never())
                .updateOrderStatus(Mockito.anyLong(), Mockito.anyString());
    }

    /* --------------------------------
       UPDATE ORDER STATUS - RUNTIME EXCEPTION
       -------------------------------- */
    @Test
    void updateOrderStatus_runtimeException() throws Exception {

        Order existingOrder = Mockito.mock(Order.class);
        ConsumerProfile consumer = Mockito.mock(ConsumerProfile.class);
        Mockito.when(existingOrder.getConsumer()).thenReturn(consumer);
        Mockito.when(consumer.getConsumerId()).thenReturn(1L);

        Mockito.when(consumerOrderService.getOrderById(10L))
                .thenReturn(Optional.of(existingOrder));
        Mockito.when(consumerOrderService.updateOrderStatus(10L, "CANCELLED"))
                .thenThrow(new RuntimeException("Order update failed"));

        mockMvc.perform(patch("/api/consumer/orders/order/{orderId}/status", 10L)
                        .sessionAttr("USER_ID", 1L)
                        .param("status", "CANCELLED"))
                .andExpect(status().isNotFound());
    }
}
