package com.frh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frh.backend.Model.Order;
import com.frh.backend.repository.ConsumerOrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsumerOrderServiceTest {

  @Mock private ConsumerOrderRepository consumerOrderRepository;

  @InjectMocks private ConsumerOrderService consumerOrderService;

  @Test
  void basicQueryMethods_delegateToRepository() {
    Order order = new Order();

    when(consumerOrderRepository.findAll()).thenReturn(List.of(order));
    when(consumerOrderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(consumerOrderRepository.findByConsumer_ConsumerIdOrderByCreatedAtDesc(2L))
        .thenReturn(List.of(order));
    when(consumerOrderRepository.findByStatusOrderByCreatedAtDesc("PENDING"))
        .thenReturn(List.of(order));
    when(consumerOrderRepository.findByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(
            2L, "PENDING"))
        .thenReturn(List.of(order));

    assertEquals(1, consumerOrderService.getAllOrders().size());
    assertEquals(true, consumerOrderService.getOrderById(1L).isPresent());
    assertEquals(1, consumerOrderService.getOrdersByConsumerId(2L).size());
    assertEquals(1, consumerOrderService.getOrdersByStatus("PENDING").size());
    assertEquals(1, consumerOrderService.getOrdersByConsumerIdAndStatus(2L, "PENDING").size());
  }

  @Test
  void createOrder_saves() {
    Order order = new Order();
    when(consumerOrderRepository.save(order)).thenReturn(order);

    Order result = consumerOrderService.createOrder(order);

    assertEquals(order, result);
  }

  @Test
  void updateOrder_success() {
    Order existing = new Order();
    existing.setOrderId(1L);
    existing.setStatus("PENDING");

    Order updated = new Order();
    updated.setStatus("ACCEPTED");
    updated.setPickupSlotStart(LocalDateTime.now().plusHours(1));
    updated.setPickupSlotEnd(LocalDateTime.now().plusHours(2));
    updated.setTotalAmount(new BigDecimal("20.00"));
    updated.setCurrency("SGD");
    updated.setCancelReason("NA");

    when(consumerOrderRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(consumerOrderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = consumerOrderService.updateOrder(1L, updated);

    assertEquals("ACCEPTED", result.getStatus());
    assertEquals("SGD", result.getCurrency());
    assertEquals("NA", result.getCancelReason());
    assertEquals(new BigDecimal("20.00"), result.getTotalAmount());
  }

  @Test
  void updateOrder_notFound_throws() {
    when(consumerOrderRepository.findById(88L)).thenReturn(Optional.empty());

    RuntimeException ex =
        assertThrows(
            RuntimeException.class, () -> consumerOrderService.updateOrder(88L, new Order()));

    assertEquals("Order not found with id: 88", ex.getMessage());
  }

  @Test
  void deleteOrder_deletesById() {
    consumerOrderService.deleteOrder(5L);

    verify(consumerOrderRepository).deleteById(5L);
  }

  @Test
  void updateOrderStatus_success() {
    Order existing = new Order();
    existing.setOrderId(1L);
    existing.setStatus("PENDING");

    when(consumerOrderRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(consumerOrderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = consumerOrderService.updateOrderStatus(1L, "COMPLETED");

    assertEquals("COMPLETED", result.getStatus());
    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(consumerOrderRepository).save(captor.capture());
    assertEquals("COMPLETED", captor.getValue().getStatus());
  }

  @Test
  void updateOrderStatus_notFound_throws() {
    when(consumerOrderRepository.findById(77L)).thenReturn(Optional.empty());

    RuntimeException ex =
        assertThrows(
            RuntimeException.class, () -> consumerOrderService.updateOrderStatus(77L, "CANCELLED"));

    assertEquals("Order not found with id: 77", ex.getMessage());
  }
}
