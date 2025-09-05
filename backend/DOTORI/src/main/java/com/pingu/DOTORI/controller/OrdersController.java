package com.pingu.DOTORI.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pingu.DOTORI.dto.OrdersRequestDTO;
import com.pingu.DOTORI.dto.OrdersResponseDTO;
import com.pingu.DOTORI.service.OrdersService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersController {

        private final OrdersService ordersService;

    /** ✅ 통합 주문 생성 - 단일/장바구니 자동 구분 */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrdersRequestDTO request,
                                        HttpServletRequest httpReq) {
        try {
            System.out.println("받은 주문 요청: " + request.toString());

            // 장바구니 주문인지 단일 주문인지 구분
            if (request.getCartIds() != null && !request.getCartIds().isEmpty()) {
                // 장바구니 기반 주문
                List<OrdersResponseDTO> response = ordersService.createOrderFromCart(request, httpReq);
                return ResponseEntity.ok(response);
            } else {
                // 단일 상품 주문
                OrdersResponseDTO response = ordersService.createSingleOrder(request, httpReq);
                return ResponseEntity.ok(response);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("잘못된 요청: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("주문 처리 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

    /** ✅ 장바구니 기반 주문 생성 (별도 엔드포인트) */
    @PostMapping("/cart")
    public ResponseEntity<List<OrdersResponseDTO>> createOrderFromCart(@RequestBody OrdersRequestDTO request,
                                                                       HttpServletRequest httpReq) {
    	System.out.println("cart METHOD -------------------------------------------------------------------------");
        List<OrdersResponseDTO> saved = ordersService.createOrderFromCart(request, httpReq);
        return ResponseEntity.ok(saved);
    }

    /** ✅ 내 주문 내역 조회 */
    @GetMapping("/me")
    public List<OrdersResponseDTO> getMyOrders(HttpServletRequest httpReq) {
        return ordersService.getMyOrders(httpReq);
    }

    /** ✅ 주문 단건 조회 */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrdersResponseDTO> getOrderById(@PathVariable Long orderId,
                                                          HttpServletRequest httpReq) {
        return ResponseEntity.ok(ordersService.getOrderById(orderId, httpReq));
    }
}
