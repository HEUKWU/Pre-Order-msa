package com.heukwu.orderservice.wishlist.controller.dto;

import lombok.Builder;

@Builder
public record WishListUpdateRequestDto(
        long wishlistProductId,
        int quantity
) { }
