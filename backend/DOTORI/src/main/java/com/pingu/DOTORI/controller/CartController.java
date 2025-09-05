// CartController.java
package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.AddCartRequest;
import com.pingu.DOTORI.dto.CartResponseDTO;
import com.pingu.DOTORI.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** 내 장바구니 조회 */
    @GetMapping("/me")
    public ResponseEntity<List<CartResponseDTO>> getMyCart(HttpServletRequest req) {
        return ResponseEntity.ok(cartService.getMyCart(req));
    }

    /** 장바구니 담기 */
    @PostMapping
    public ResponseEntity<CartResponseDTO> addToCart(HttpServletRequest req,
                                                     @RequestBody AddCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(req, request));
    }

    /** 수량 변경 */
    @PutMapping("/{id}")
    public ResponseEntity<CartResponseDTO> updateQuantity(HttpServletRequest req,
                                                          @PathVariable Long id,
                                                          @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(req, id, quantity));
    }

    /** 개별 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeCart(HttpServletRequest req, @PathVariable Long id) {
        cartService.removeCart(req, id);
        return ResponseEntity.noContent().build();
    }

    /** 전체 삭제 */
    @DeleteMapping("/me")
    public ResponseEntity<Void> clearCart(HttpServletRequest req) {
        cartService.clearCart(req);
        return ResponseEntity.noContent().build();
    }
}
