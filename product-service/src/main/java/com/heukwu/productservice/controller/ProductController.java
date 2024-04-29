package com.heukwu.productservice.controller;

import com.heukwu.common.dto.ApiResponse;
import com.heukwu.productservice.controller.dto.ProductRequestDto;
import com.heukwu.productservice.controller.dto.ProductResponseDto;
import com.heukwu.productservice.controller.dto.ProductSearch;
import com.heukwu.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "product", description = "상품")
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 목록 조회")
    @GetMapping("/product")
    public ApiResponse<List<ProductResponseDto>> getProductList(ProductSearch search, @RequestParam int size, Long cursorId) {
        List<ProductResponseDto> productResponseDtoList = productService.getProductList(search, size, cursorId);

        return ApiResponse.success(productResponseDtoList);
    }

    @Operation(summary = "상품 상세 조회")
    @GetMapping("/product/{productId}")
    public ApiResponse<ProductResponseDto> getProduct(@PathVariable Long productId) {
        ProductResponseDto productResponseDto = productService.getProduct(productId);

        return ApiResponse.success(productResponseDto);
    }

    @Operation(summary = "상품 등록")
    @PostMapping("/product")
    public ApiResponse<ProductResponseDto> createProduct(@RequestBody ProductRequestDto requestDto) {
        ProductResponseDto productResponseDto = productService.createProduct(requestDto);

        return ApiResponse.success(productResponseDto);
    }
}
