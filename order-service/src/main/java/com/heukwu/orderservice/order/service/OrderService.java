package com.heukwu.orderservice.order.service;

import com.heukwu.common.exception.BusinessException;
import com.heukwu.common.exception.ErrorMessage;
import com.heukwu.common.exception.NotFoundException;
import com.heukwu.orderservice.order.controller.dto.OrderRequestDto;
import com.heukwu.orderservice.order.controller.dto.OrderResponseDto;
import com.heukwu.orderservice.order.entity.Order;
import com.heukwu.orderservice.order.entity.OrderProduct;
import com.heukwu.orderservice.order.entity.OrderStatus;
import com.heukwu.orderservice.order.repository.OrderProductRepository;
import com.heukwu.orderservice.order.repository.OrderRepository;
import com.heukwu.productservice.entity.Product;
import com.heukwu.common.user.User;
import com.heukwu.productservice.repository.ProductRepository;
import com.heukwu.orderservice.wishlist.entity.Wishlist;
import com.heukwu.orderservice.wishlist.entity.WishlistProduct;
import com.heukwu.orderservice.wishlist.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;

    @Transactional
    public OrderResponseDto orderProduct(OrderRequestDto requestDto, User user) {
        Product product = productRepository.findById(requestDto.productId()).orElseThrow(
                () -> new NotFoundException(ErrorMessage.NOT_FOUND_PRODUCT)
        );

        OrderProduct orderProduct = createOrderProduct(product);

        // 주문으로 인한 수량 감소
        int quantity = requestDto.quantity();
        product.decreaseQuantity(quantity);

        Order order = Order.builder()
                .totalPrice(product.getPrice() * requestDto.quantity())
                .quantity(quantity)
                .userId(user.getId())
                .orderProduct(orderProduct)
                .status(OrderStatus.CREATED)
                .build();

        orderRepository.save(order);

        return OrderResponseDto.of(order);
    }

    public List<OrderResponseDto> getUserOrderInfo(User user) {
        List<Order> orderList = orderRepository.findAllByUserId(user.getId());

        return orderList.stream().map(OrderResponseDto::of).toList();
    }

    @Transactional
    public List<OrderResponseDto> orderWishlist(User user) {
        Wishlist wishlist = wishlistRepository.findWishlistByUserId(user.getId()).orElseThrow(
                () -> new NotFoundException(ErrorMessage.EMPTY_WISHLIST)
        );

        List<WishlistProduct> wishlistProducts = wishlist.getWishlistProducts();

        // 주문 생성
        return createOrder(user, wishlistProducts);
    }

    @Transactional
    public void cancelOrder(User user) {
        List<Order> orderList = orderRepository.findAllByUserId(user.getId());

        for (Order order : orderList) {
            // 배송이 시작되었다면 취소 불가
            if (order.isNotCancelable()) {
                throw new BusinessException(ErrorMessage.CANNOT_CANCEL_ORDER);
            }

            order.cancelOrder();

            // 상품 재고 복구
            Product product = productRepository.findById(order.getOrderProduct().getProductId()).orElseThrow(
                    () -> new NotFoundException(ErrorMessage.NOT_FOUND_PRODUCT)
            );

            product.increaseQuantity(order.getQuantity());
        }
    }

    @Transactional
    public void returnOrder(User user) {
        List<Order> orderList = orderRepository.findAllByUserId(user.getId());

        for (Order order : orderList) {
            if (order.isNotReturnableStatus()) {
                throw new BusinessException(ErrorMessage.CANNOT_RETURN_ORDER_STATUS);
            }
            if (order.isNotReturnableDate()) {
                throw new BusinessException(ErrorMessage.CANNOT_RETURN_ORDER_DATE);
            }

            order.returnOrder();
        }
    }


    // 주문 상태 변경
    @Transactional
    public void updateOrderStatus() {
        List<Order> createdOrders = orderRepository.findAllByStatusAndModifiedAtBefore(OrderStatus.CREATED, LocalDateTime.now().minusDays(1));

        // 생성 후 하루 지난 주문 배송중 처리
        for (Order order : createdOrders) {
            order.updateStatus(OrderStatus.SHIPPING);
        }

        List<Order> shippingOrders = orderRepository.findAllByStatusAndModifiedAtBefore(OrderStatus.SHIPPING, LocalDateTime.now().minusDays(1));

        // 배송 시작 후 하루 지난 주문 배송완료 처리
        for (Order order : shippingOrders) {
            order.updateStatus(OrderStatus.COMPLETED);
        }

        List<Order> returnedOrders = orderRepository.findAllByStatusAndModifiedAtBefore(OrderStatus.RETURNING, LocalDateTime.now().minusDays(1));

        // 반품 완료 후 하루 지난 상품 재고 복구
        for (Order order : returnedOrders) {
            Product product = productRepository.findById(order.getOrderProduct().getProductId()).orElseThrow(
                    () -> new NotFoundException(ErrorMessage.NOT_FOUND_PRODUCT)
            );

            product.increaseQuantity(order.getQuantity());
            order.updateStatus(OrderStatus.RETURNED);
        }
    }

    private List<OrderResponseDto> createOrder(User user, List<WishlistProduct> wishlistProducts) {
        List<OrderResponseDto> orderResponseDtoList = new ArrayList<>();

        for (WishlistProduct wishlistProduct : wishlistProducts) {
            // 장바구니 상품
            Product product = productRepository.findById(wishlistProduct.getProductId()).orElseThrow(
                    () -> new NotFoundException(ErrorMessage.NOT_FOUND_PRODUCT));

            // 주문에 따른 상품 수량 감소
            product.decreaseQuantity(wishlistProduct.getQuantity());

            OrderProduct orderProduct = createOrderProduct(product);

            // 주문 생성
            Order order = Order.builder()
                    .totalPrice(wishlistProduct.getQuantity() * product.getPrice())
                    .quantity(wishlistProduct.getQuantity())
                    .userId(user.getId())
                    .orderProduct(orderProduct)
                    .status(OrderStatus.CREATED)
                    .build();

            orderRepository.save(order);

            // 장바구니 상품 삭제
            wishlistProduct.delete();
            orderResponseDtoList.add(OrderResponseDto.of(order));
        }

        return orderResponseDtoList;
    }

    private OrderProduct createOrderProduct(Product product) {
        OrderProduct orderProduct = OrderProduct.builder()
                .productId(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();

        orderProductRepository.save(orderProduct);

        return orderProduct;
    }
}
