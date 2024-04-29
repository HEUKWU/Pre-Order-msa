package com.heukwu.orderservice.wishlist.service;

import com.heukwu.common.exception.ErrorMessage;
import com.heukwu.common.exception.NotFoundException;
import com.heukwu.productservice.entity.Product;
import com.heukwu.productservice.repository.ProductRepository;
import com.heukwu.common.user.User;
import com.heukwu.orderservice.wishlist.controller.dto.WishListAddRequestDto;
import com.heukwu.orderservice.wishlist.controller.dto.WishListUpdateRequestDto;
import com.heukwu.orderservice.wishlist.controller.dto.WishlistResponseDto;
import com.heukwu.orderservice.wishlist.entity.Wishlist;
import com.heukwu.orderservice.wishlist.entity.WishlistProduct;
import com.heukwu.orderservice.wishlist.repository.WishlistProductRepository;
import com.heukwu.orderservice.wishlist.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final ProductRepository productRepository;
    private final WishlistProductRepository wishlistProductRepository;
    private final WishlistRepository wishlistRepository;

    @Transactional
    public WishlistResponseDto addWishlist(WishListAddRequestDto requestDto, User user) {
        Product product = productRepository.findById(requestDto.productId()).orElseThrow(
                () -> new NotFoundException(ErrorMessage.NOT_FOUND_PRODUCT)
        );

        Wishlist wishlist = wishlistRepository.findWishlistByUserId(user.getId()).orElse(
                Wishlist.builder().userId(user.getId()).build()
        );

        WishlistProduct wishlistProduct = createWishlistProduct(requestDto, wishlist, product);

        return WishlistResponseDto.of(wishlistProduct);
    }

    public List<WishlistResponseDto> getUserWishlist(User user) {
        Wishlist wishlist = wishlistRepository.findWishlistByUserId(user.getId()).orElseThrow(
                () -> new NotFoundException(ErrorMessage.EMPTY_WISHLIST)
        );

        List<WishlistProduct> wishlistProducts = wishlist.getWishlistProducts();
        List<WishlistProduct> notDeletedWishlistProducts = wishlistProducts.stream()
                .filter(wishlistProduct -> !wishlistProduct.isDeleted())
                .toList();

        return notDeletedWishlistProducts.stream().map(WishlistResponseDto::of).toList();
    }

    @Transactional
    public WishlistResponseDto updateWishlist(WishListUpdateRequestDto requestDto) {
        WishlistProduct wishlistProduct = wishlistProductRepository.findById(requestDto.wishlistProductId()).orElseThrow(
                () -> new NotFoundException(ErrorMessage.NOT_FOUND_WISHLIST_PRODUCT)
        );

        wishlistProduct.updateQuantity(requestDto.quantity());

        return WishlistResponseDto.of(wishlistProduct);
    }

    private WishlistProduct createWishlistProduct(WishListAddRequestDto requestDto, Wishlist wishlist, Product product) {
        Optional<WishlistProduct> optionalWishlistProduct = wishlistProductRepository.findWishlistProductByWishlistIdAndProductIdAndDeletedFalse(wishlist.getId(), product.getId());

        WishlistProduct wishlistProduct;
        // 해당 상품이 이미 장바구니에 있다면
        if (optionalWishlistProduct.isPresent()) {
            wishlistProduct = optionalWishlistProduct.get();
            // 수량 업데이트
            wishlistProduct.increaseQuantity(requestDto.quantity());
        } else {
            wishlistProduct = WishlistProduct.builder()
                    .wishlist(wishlist)
                    .productId(product.getId())
                    .quantity(requestDto.quantity())
                    .build();

            wishlistProductRepository.save(wishlistProduct);
        }

        return wishlistProduct;
    }
}
