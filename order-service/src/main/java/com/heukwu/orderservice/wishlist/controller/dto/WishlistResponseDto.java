package com.heukwu.orderservice.wishlist.controller.dto;

import com.heukwu.orderservice.wishlist.entity.WishlistProduct;
import lombok.Builder;

@Builder
public record WishlistResponseDto(
        long productId,
        int quantity
) {
    public static WishlistResponseDto of(WishlistProduct wishlistProduct) {
        return WishlistResponseDto.builder()
                .productId(wishlistProduct.getProductId())
                .quantity(wishlistProduct.getQuantity())
                .build();
    }
}
