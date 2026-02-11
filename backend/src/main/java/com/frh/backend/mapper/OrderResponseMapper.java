package com.frh.backend.mapper;

import com.frh.backend.Model.Order;
import com.frh.backend.Model.OrderItem;
import com.frh.backend.dto.OrderResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderResponseMapper {

    public List<OrderResponseDto> toOrderResponseList(List<Order> orders) {
        if (orders == null) {
            return List.of();
        }
        return orders.stream()
            .map(this::toOrderResponse)
            .collect(Collectors.toList());
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
            List<OrderResponseDto.OrderItemDto> itemDtos = order.getOrderItems().stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
            dto.setOrderItems(itemDtos);
        }

        if (order.getPickupToken() != null) {
            dto.setPickupTokenHash(order.getPickupToken().getQrTokenHash());
            dto.setPickupTokenExpiresAt(order.getPickupToken().getExpiresAt());
        }

        return dto;
    }

    private OrderResponseDto.OrderItemDto toOrderItemResponse(OrderItem item) {
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
            itemDto.setListing(listingDto);
        }

        return itemDto;
    }
}
