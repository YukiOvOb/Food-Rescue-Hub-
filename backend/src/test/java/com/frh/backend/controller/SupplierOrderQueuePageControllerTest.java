package com.frh.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.frh.backend.model.Order;
import com.frh.backend.dto.OrderSummaryDTO;
import com.frh.backend.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SupplierOrderQueuePageController.class)
@Import(SupplierOrderQueuePageControllerTest.TestViewConfig.class)
@TestPropertySource(properties = "spring.thymeleaf.enabled=false")
class SupplierOrderQueuePageControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private OrderService orderService;

  /* --------------------------------
  RENDER ORDER QUEUE – DEFAULT STATUS
  -------------------------------- */
  @Test
  void showOrderQueue_defaultStatus() throws Exception {

    OrderSummaryDTO dto = new OrderSummaryDTO();
    dto.setOrderId(1L);

    Mockito.when(orderService.getOrderQueue(10L, "PENDING")).thenReturn(List.of(dto));

    mockMvc
        .perform(get("/supplier/order-queue/{storeId}", 10L))
        .andExpect(status().isOk())
        .andExpect(view().name("supplier/order-queue"))
        .andExpect(model().attribute("storeId", 10L))
        .andExpect(model().attribute("activeStatus", "PENDING"))
        .andExpect(model().attributeExists("orders"));
  }

  /* --------------------------------
  RENDER ORDER QUEUE – CUSTOM STATUS
  -------------------------------- */
  @Test
  void showOrderQueue_customStatus() throws Exception {

    Mockito.when(orderService.getOrderQueue(10L, "ACCEPTED")).thenReturn(List.of());

    mockMvc
        .perform(get("/supplier/order-queue/{storeId}", 10L).param("status", "ACCEPTED"))
        .andExpect(status().isOk())
        .andExpect(view().name("supplier/order-queue"))
        .andExpect(model().attribute("activeStatus", "ACCEPTED"));
  }

  /* --------------------------------
  ACCEPT ORDER – SUCCESS
  -------------------------------- */
  @Test
  void acceptOrder_success() throws Exception {

    Mockito.when(orderService.acceptOrder(100L)).thenReturn(new Order());

    mockMvc
        .perform(post("/supplier/order-queue/{storeId}/accept", 10L).param("orderId", "100"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/supplier/order-queue/10"))
        .andExpect(flash().attributeExists("successMsg"));
  }

  /* --------------------------------
  ACCEPT ORDER – FAILURE
  -------------------------------- */
  @Test
  void acceptOrder_failure() throws Exception {

    Mockito.doThrow(new RuntimeException("Cannot accept order"))
        .when(orderService)
        .acceptOrder(100L);

    mockMvc
        .perform(post("/supplier/order-queue/{storeId}/accept", 10L).param("orderId", "100"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/supplier/order-queue/10"))
        .andExpect(flash().attributeExists("errorMsg"));
  }

  /* --------------------------------
  REJECT ORDER – SUCCESS
  -------------------------------- */
  @Test
  void rejectOrder_success() throws Exception {

    Mockito.when(orderService.rejectOrder(200L, "Out of stock")).thenReturn(new Order());

    mockMvc
        .perform(
            post("/supplier/order-queue/{storeId}/reject", 10L)
                .param("orderId", "200")
                .param("reason", "Out of stock"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/supplier/order-queue/10"))
        .andExpect(flash().attributeExists("successMsg"));
  }

  /* --------------------------------
  CANCEL ORDER – SUCCESS
  -------------------------------- */
  @Test
  void cancelOrder_success() throws Exception {

    Mockito.when(orderService.cancelAcceptedOrder(300L, "Customer request"))
        .thenReturn(new Order());

    mockMvc
        .perform(
            post("/supplier/order-queue/{storeId}/cancel", 10L)
                .param("orderId", "300")
                .param("reason", "Customer request"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/supplier/order-queue/10"))
        .andExpect(flash().attributeExists("successMsg"));
  }

  @TestConfiguration
  static class TestViewConfig {
    @Bean
    @Primary
    ViewResolver testViewResolver() {
      return new ViewResolver() {
        @Override
        public View resolveViewName(String viewName, Locale locale) {
          return new AbstractView() {
            @Override
            protected void renderMergedOutputModel(
                Map<String, Object> model,
                HttpServletRequest request,
                HttpServletResponse response) {
              response.setStatus(HttpServletResponse.SC_OK);
            }
          };
        }
      };
    }
  }
}

