package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.AddCartRequest;
import com.pingu.DOTORI.dto.CartResponse;
import com.pingu.DOTORI.entity.Cart;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.repository.UsersRepository;
import com.pingu.DOTORI.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 장바구니 컨트롤러 (JWT 토큰 기반으로 수정)

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UsersRepository usersRepository;

    // 장바구니 담기
    @PostMapping
    public ResponseEntity<Cart> add(@RequestBody AddCartRequest request, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).build(); // 로그인 안 된 경우
        }
        return ResponseEntity.ok(cartService.addCart(userId, request.getItemDetailsId()));
    }

    // 장바구니 조회 (최신순)
    @GetMapping("/me")
    public ResponseEntity<List<CartResponse>> getCart(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
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
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(cartService.updateQuantity(cartId, quantity, userId));
    }

    // 장바구니 전체 비우기
    @DeleteMapping("/me")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    // 개별 상품 삭제 (본인 확인 포함)
    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long cartId, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        cartService.removeItem(cartId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * JWT Authentication에서 userId 추출하는 헬퍼 메서드
     * JWT 토큰의 subject(email)로 사용자를 조회하여 userId를 반환
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return null;
        }

        // JWT에서 추출한 email (subject)
        String email = ((UserDetails) principal).getUsername();
        
        // email로 사용자 조회하여 userId 반환
        return usersRepository.findByEmail(email)
                .map(Users::getId)  // Users 엔티티의 getId() 메서드 사용
                .orElse(null);
    }
}