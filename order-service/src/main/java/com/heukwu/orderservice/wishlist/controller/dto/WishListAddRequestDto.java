package com.heukwu.orderservice.wishlist.controller.dto;

import lombok.Builder;

@Builder
public record WishListAddRequestDto(
        long productId,
        int quantity
) { }
