package com.pingu.DOTORI.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.pingu.DOTORI.dto.WishListItemDto;
import com.pingu.DOTORI.entity.ItemDetails;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.entity.WishList;
import com.pingu.DOTORI.repository.ItemDetailsRepository;
import com.pingu.DOTORI.repository.UsersRepository;
import com.pingu.DOTORI.security.JwtProvider;
import com.pingu.DOTORI.service.WishListService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService wishListService;
    private final JwtProvider jwtProvider;
    private final UsersRepository usersRepository;
    private final ItemDetailsRepository itemDetailsRepository;

    @GetMapping
    public ResponseEntity<?> getMyWishlist(HttpServletRequest req) {
        Users me = getMe(req);
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        }

        List<WishList> liked = wishListService.getLikedByUserId(me.getId());
        List<WishListItemDto> dto = liked.stream().map(w -> WishListItemDto.builder()
            .wishlistId(w.getId())
            .itemId(w.getItemDetails().getItemId())
            .itemImg(safeImg(w))
            .name(w.getItemDetails().getItem().getName())
            .cost(w.getItemDetails().getItem().getCost().intValue())
            .build()).collect(Collectors.toList());
        return ResponseEntity.ok(dto); 
    }

    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<?> unlike(@PathVariable Long wishlistId, HttpServletRequest req) {
        Users me = getMe(req);
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        }
        boolean ok = wishListService.unlikeByWishlistId(wishlistId, me.getId());
        if (ok) return ResponseEntity.ok(Map.of("success", true));
        return ResponseEntity.badRequest().body(Map.of("success", false));
    }

    private String safeImg(WishList w) {
        return w.getItemDetails().getItem() != null ? w.getItemDetails().getItem().getImgUrl() : null;
    }

    @PostMapping
    public ResponseEntity<?> likeItem(@RequestBody Map<String, Long> body, HttpServletRequest req) {
        Users me = getMe(req);
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        }
        
        Long itemId = body.get("itemId");
        if (itemId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "itemId is required"));
        }
        
        try {
            ItemDetails itemDetails = itemDetailsRepository.findByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with itemId: " + itemId));
            
            WishList wishList = wishListService.toggleLike(me, itemDetails);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "wishlistId", wishList.getId(),
                "isLiked", wishList.getIsLiked()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test-items")
    public ResponseEntity<?> getTestItems() {
        try {
            // 테스트용 상품 목록 반환
            List<Map<String, Object>> testItems = new ArrayList<>();
            
            // 실제 DB에서 상품 정보 가져오기
            List<ItemDetails> allItems = itemDetailsRepository.findAll();
            
            for (ItemDetails item : allItems) {
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("itemDetailsId", item.getItemId());
                itemInfo.put("itemName", item.getItem() != null ? item.getItem().getName() : "Unknown");
                itemInfo.put("cost", item.getCost());
                itemInfo.put("status", item.getStatus());
                testItems.add(itemInfo);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "items", testItems,
                "totalCount", testItems.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Users getMe(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String token = authHeader.substring(7);
            String email = jwtProvider.getSubject(token);
            return usersRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}


