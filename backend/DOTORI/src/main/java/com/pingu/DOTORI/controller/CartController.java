package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.AddCartRequest;
import com.pingu.DOTORI.dto.CartResponse;
import com.pingu.DOTORI.entity.Cart;
import com.pingu.DOTORI.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 장바구니 컨트롤러 (세션 로그인 기반)

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 장바구니 담기
    @PostMapping
    public ResponseEntity<Cart> add(@RequestBody AddCartRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build(); // 로그인 안 된 경우
        }
        return ResponseEntity.ok(cartService.addCart(userId, request.getItemDetailsId()));
    }

    // 장바구니 조회 (최신순)
    @GetMapping("/me")
    public ResponseEntity<List<CartResponse>> getCart(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.getCartByUser(userId));
    }

    // 장바구니 수량 변경
    @PutMapping("/{cartId}")
    public ResponseEntity<Cart> updateQuantity(
            @PathVariable Long cartId,
            @RequestParam int quantity,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(cartService.updateQuantity(cartId, quantity, userId));
    }

    // 장바구니 전체 비우기
    @DeleteMapping("/me")
    public ResponseEntity<Void> clearCart(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    // 개별 상품 삭제 (본인 확인 포함)
    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long cartId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        cartService.removeItem(cartId, userId);
        return ResponseEntity.noContent().build();
    }
}
