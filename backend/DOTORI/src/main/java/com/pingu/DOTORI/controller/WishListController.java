package com.pingu.DOTORI.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pingu.DOTORI.entity.WishList;
import com.pingu.DOTORI.security.JwtProvider;
import com.pingu.DOTORI.service.WishListService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WishListController {

   private final WishListService wishListService;
   private final JwtProvider jwtProvider;

   @PostMapping("/api/wishlist/toggle")
   public ResponseEntity<?> toggleWishList(@RequestBody Map<String, Long> request,
                                           HttpServletRequest req) {
       String authHeader = req.getHeader("Authorization");
       if (authHeader != null && authHeader.startsWith("Bearer ")) {
           try {
               String token = authHeader.substring(7);
               String email = jwtProvider.getSubject(token);

               Long itemId = request.get("itemId");
               if (itemId != null) {
                   boolean result = wishListService.toggleWishList(email, itemId);
                   return ResponseEntity.ok(Map.of("added", result));
               }
           } catch (Exception e) {
               System.out.println("JWT 파싱 실패: " + e.getMessage());
           }
       }
       
       return ResponseEntity.badRequest().body(Map.of("error", "위시리스트 토글 실패"));
   }

   @GetMapping("/api/wishlist")
   public ResponseEntity<?> getUserWishList(HttpServletRequest req) {
       String authHeader = req.getHeader("Authorization");
       if (authHeader != null && authHeader.startsWith("Bearer ")) {
           try {
               String token = authHeader.substring(7);
               String email = jwtProvider.getSubject(token);

               List<WishList> wishList = wishListService.getUserWishList(email);
               return ResponseEntity.ok(wishList);
           } catch (Exception e) {
               System.out.println("JWT 파싱 실패: " + e.getMessage());
           }
       }

       return ResponseEntity.ok(Map.of("authenticated", false));
   }
}