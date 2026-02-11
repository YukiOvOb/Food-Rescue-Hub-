package com.frh.backend.mapper;

import com.frh.backend.dto.OrderResponseDto;
import com.frh.backend.model.Order;
import com.frh.backend.model.OrderItem;
import com.frh.backend.repository.ListingReviewRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderResponseMapper {

  private final ListingReviewRepository listingReviewRepository;

  public List<OrderResponseDto> toOrderResponseList(List<Order> orders) {
    if (orders == null) {
      return List.of();
    }
    return orders.stream().map(this::toOrderResponse).collect(Collectors.toList());
  }

  public OrderResponseDto toOrderResponse(Order order) {
    OrderResponseDto dto = new OrderResponseDto();
    if (order == null) {
      return dto;
    }

    dto.setOrderId(order.getOrderId());
    dto.setStatus(order.getStatus());
    dto.setTotalAmount(order.getTotalAmount());
    dto.setCurrency(order.getCurrency());
    dto.setPickupSlotStart(order.getPickupSlotStart());
    dto.setPickupSlotEnd(order.getPickupSlotEnd());
    dto.setCancelReason(order.getCancelReason());
    dto.setCreatedAt(order.getCreatedAt());
    dto.setUpdatedAt(order.getUpdatedAt());

    if (order.getStore() != null) {
      OrderResponseDto.StoreDto storeDto = new OrderResponseDto.StoreDto();
      storeDto.setStoreId(order.getStore().getStoreId());
      storeDto.setStoreName(order.getStore().getStoreName());
      storeDto.setAddressLine(order.getStore().getAddressLine());
      storeDto.setPostalCode(order.getStore().getPostalCode());
      storeDto.setLat(order.getStore().getLat());
      storeDto.setLng(order.getStore().getLng());
      dto.setStore(storeDto);
    }

    if (order.getConsumer() != null) {
      OrderResponseDto.ConsumerDto consumerDto = new OrderResponseDto.ConsumerDto();
      consumerDto.setConsumerId(order.getConsumer().getConsumerId());
      consumerDto.setDisplayName(order.getConsumer().getDisplayName());
      consumerDto.setDefaultLat(order.getConsumer().getDefault_lat());
      consumerDto.setDefaultLng(order.getConsumer().getDefault_lng());
      dto.setConsumer(consumerDto);
    }

    if (order.getOrderItems() != null) {
      Long orderId = order.getOrderId();
      Long consumerId = order.getConsumer() != null ? order.getConsumer().getConsumerId() : null;
      List<OrderResponseDto.OrderItemDto> itemDtos =
          order.getOrderItems().stream()
              .map(item -> toOrderItemResponse(item, orderId, consumerId))
              .collect(Collectors.toList());
      dto.setOrderItems(itemDtos);
    }

    if (order.getPickupToken() != null) {
      dto.setPickupTokenHash(order.getPickupToken().getQrTokenHash());
      dto.setPickupTokenExpiresAt(order.getPickupToken().getExpiresAt());
    }

    return dto;
  }

  private OrderResponseDto.OrderItemDto toOrderItemResponse(OrderItem item, Long orderId, Long consumerId) {
    OrderResponseDto.OrderItemDto itemDto = new OrderResponseDto.OrderItemDto();
    if (item == null) {
      return itemDto;
    }

    itemDto.setOrderItemId(item.getOrderItemId());
    itemDto.setQuantity(item.getQuantity());
    itemDto.setUnitPrice(item.getUnitPrice());
    itemDto.setLineTotal(item.getLineTotal());

    if (item.getListing() != null) {
      OrderResponseDto.ListingDto listingDto = new OrderResponseDto.ListingDto();
      listingDto.setListingId(item.getListing().getListingId());
      listingDto.setTitle(item.getListing().getTitle());

      // Check if this listing has been reviewed
      boolean hasReviewed = false;
      if (orderId != null && consumerId != null) {
        hasReviewed = listingReviewRepository.existsByOrder_OrderIdAndListing_ListingIdAndConsumer_ConsumerId(
            orderId, item.getListing().getListingId(), consumerId);
      }
      listingDto.setHasReviewed(hasReviewed);

      itemDto.setListing(listingDto);
    }

    return itemDto;
  }
}
