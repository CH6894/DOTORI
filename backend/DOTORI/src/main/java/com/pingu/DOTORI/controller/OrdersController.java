package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.OrderRequest;
import com.pingu.DOTORI.dto.OrderResponse;
import com.pingu.DOTORI.entity.Orders;
import com.pingu.DOTORI.service.OrdersService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersController {

        private final OrdersService ordersService;

        // 주문 생성 (장바구니 → 주문)
        @PostMapping
        public ResponseEntity<List<OrderResponse>> createOrders(
                        @RequestBody OrderRequest request,
                        HttpSession session) {

                Long userId = (Long) session.getAttribute("userId");
                if (userId == null) {
                        return ResponseEntity.status(401).build();
                }

                List<Orders> orders = ordersService.createOrdersFromCart(userId, request);

                // Orders → OrderResponse 변환
                List<OrderResponse> response = orders.stream()
                                .map(order -> OrderResponse.builder()
                                                .orderId(order.getId())
                                                .itemName(order.getItemDetails().getItem() != null
                                                                ? order.getItemDetails().getItem().getName()
                                                                : null)
                                                .price(order.getItemDetails().getCost() != null
                                                                ? order.getItemDetails().getCost().intValue()
                                                                : 0)
                                                .quantity(1) // ✅ Cart → Orders 변환 시 수량 관리 (기본 1)
                                                .depositerName(order.getDepositerName())
                                                .payMethod(order.getPayMethod())
                                                .payMessage(order.getPayMessage())
                                                .payTime(order.getPayTime())
                                                .build())
                                .collect(Collectors.toList());

                return ResponseEntity.ok(response);
        }

        // 나의 주문 내역 조회
        @GetMapping("/me")
        public ResponseEntity<List<OrderResponse>> getMyOrders(HttpSession session) {
                Long userId = (Long) session.getAttribute("userId");
                if (userId == null) {
                        return ResponseEntity.status(401).build();
                }

                List<Orders> orders = ordersService.getOrdersByUser(userId);

                List<OrderResponse> response = orders.stream()
                                .map(order -> OrderResponse.builder()
                                                .orderId(order.getId())
                                                .itemName(order.getItemDetails().getItem() != null
                                                                ? order.getItemDetails().getItem().getName()
                                                                : null)
                                                .price(order.getItemDetails().getCost() != null
                                                                ? order.getItemDetails().getCost().intValue()
                                                                : 0)
                                                .quantity(1)
                                                .depositerName(order.getDepositerName())
                                                .payMethod(order.getPayMethod())
                                                .payMessage(order.getPayMessage())
                                                .payTime(order.getPayTime())
                                                .build())
                                .collect(Collectors.toList());

                return ResponseEntity.ok(response);
        }
}
