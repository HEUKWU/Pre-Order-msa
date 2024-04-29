package com.heukwu.orderservice.order.controller.dto;

import com.heukwu.orderservice.order.entity.Order;
import com.heukwu.orderservice.order.entity.OrderStatus;
import lombok.Builder;

@Builder
public record OrderResponseDto(
        long userId,
        long productId,
        String productName,
        int quantity,
        int totalPrice,
        OrderStatus orderStatus
) {
        public static OrderResponseDto of(Order order) {
        return OrderResponseDto.builder()
                .userId(order.getUserId())
                .productId(order.getOrderProduct().getProductId())
                .productName(order.getOrderProduct().getName())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getStatus())
                .build();
    }
}
