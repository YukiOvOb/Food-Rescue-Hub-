package com.frh.backend.controller;

import com.frh.backend.dto.OrderSummaryDto;
import com.frh.backend.service.OrderService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/*supplier's order-queue page */

@Controller
@RequestMapping("/supplier/order-queue")
public class SupplierOrderQueuePageController {

  @Autowired private OrderService orderService;

  // the Render page
  @GetMapping("/{storeId}")
  public String showOrderQueue(
      @PathVariable Long storeId,
      @RequestParam(required = false, defaultValue = "PENDING") String status,
      Model model,
      RedirectAttributes redirectAttributes) {

    List<OrderSummaryDto> orders = orderService.getOrderQueue(storeId, status);

    model.addAttribute("storeId", storeId);
    model.addAttribute("activeStatus", status);
    model.addAttribute("orders", orders);

    // flash messages from redirects (after accept / reject)
    // already handled by Spring's RedirectAttributes – Thymeleaf reads them
    // automatically

    return "supplier/order-queue"; // src/main/resources/templates/supplier/order-queue.html
  }

  // Accept
  @PostMapping("/{storeId}/accept")
  public String acceptOrder(
      @PathVariable Long storeId, @RequestParam Long orderId, RedirectAttributes ra) {
    try {
      orderService.acceptOrder(orderId);
      ra.addFlashAttribute("successMsg", "Order #" + orderId + " accepted successfully.");
    } catch (Exception ex) {
      ra.addFlashAttribute("errorMsg", ex.getMessage());
    }
    return "redirect:/supplier/order-queue/" + storeId;
  }

  // Reject
  @PostMapping("/{storeId}/reject")
  public String rejectOrder(
      @PathVariable Long storeId,
      @RequestParam Long orderId,
      @RequestParam String reason,
      RedirectAttributes ra) {
    try {
      orderService.rejectOrder(orderId, reason);
      ra.addFlashAttribute("successMsg", "Order #" + orderId + " rejected.");
    } catch (Exception ex) {
      ra.addFlashAttribute("errorMsg", ex.getMessage());
    }
    return "redirect:/supplier/order-queue/" + storeId;
  }

  // Cancel (accepted order)
  @PostMapping("/{storeId}/cancel")
  public String cancelOrder(
      @PathVariable Long storeId,
      @RequestParam Long orderId,
      @RequestParam String reason,
      RedirectAttributes ra) {
    try {
      orderService.cancelAcceptedOrder(orderId, reason);
      ra.addFlashAttribute("successMsg", "Order #" + orderId + " cancelled. Stock restored.");
    } catch (Exception ex) {
      ra.addFlashAttribute("errorMsg", ex.getMessage());
    }
    return "redirect:/supplier/order-queue/" + storeId;
  }
}

/*
 * just for my reference - roughly
 * the given Routes
 * GET /supplier/order-queue/{storeId} – render the queue page
 * POST /supplier/order-queue/{storeId}/accept – accept an order (form submit)
 * POST /supplier/order-queue/{storeId}/reject – reject an order (form submit)
 * POST /supplier/order-queue/{storeId}/cancel – cancel accepted order
 */
