package com.heukwu.productservice.repository;

import com.heukwu.productservice.controller.dto.ProductSearch;
import com.heukwu.productservice.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ProductRepositoryCustom {
    Slice<Product> findBySearchOption(Long cursorId, ProductSearch search, Pageable pageable);
}
