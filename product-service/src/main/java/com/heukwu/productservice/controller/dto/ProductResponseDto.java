package com.heukwu.productservice.controller.dto;

import com.heukwu.productservice.entity.Product;
import lombok.Builder;

@Builder
public record ProductResponseDto(
        long id,
        String name,
        String description,
        int price,
        int quantity
) {
    public static ProductResponseDto toListResponseDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }

    public static ProductResponseDto of(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }
}
