package com.heukwu.productservice.controller.dto;

import lombok.Builder;

@Builder
public record ProductRequestDto(
        String name,
        String description,
        int price,
        int quantity
) { }
