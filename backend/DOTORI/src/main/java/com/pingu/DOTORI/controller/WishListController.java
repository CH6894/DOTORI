// package com.pingu.DOTORI.controller;

// import java.util.List;
// import java.util.Map;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import com.pingu.DOTORI.entity.Item;
// import com.pingu.DOTORI.entity.Users;
// import com.pingu.DOTORI.entity.WishList;
// import com.pingu.DOTORI.repository.ItemRepository;
// import com.pingu.DOTORI.repository.UsersRepository;
// import com.pingu.DOTORI.security.JwtProvider;
// import com.pingu.DOTORI.service.WishListService;

// import jakarta.servlet.http.HttpServletRequest;
// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api")
// @RequiredArgsConstructor
// public class WishListController {

//     private final WishListService wishListService;
//     private final JwtProvider jwtProvider;
//     private final UsersRepository usersRepository;
//     private final ItemRepository itemRepository;

//     /**
//      * 위시리스트 토글 (추가/제거)
//      */
//     @PostMapping("/wishlist/toggle/{itemId}")
//     public ResponseEntity<?> toggleWishList(@PathVariable String itemId, HttpServletRequest request) {
//         System.out.println("=== 위시리스트 토글 API 요청 ===");
//         System.out.println("상품 ID: " + itemId);

//         try {
//             // JWT에서 사용자 이메일 추출
//             String email = getEmailFromToken(request);
//             if (email == null) {
//                 System.out.println("인증 실패: 토큰에서 이메일을 추출할 수 없음");
//                 return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
//             }

//             // 이메일로 사용자 ID 조회 (간단한 구현 - 실제로는 UsersRepository를 주입받아 사용)
//             Long userId = getUserIdByEmail(email);
//             if (userId == null) {
//                 System.out.println("사용자를 찾을 수 없음: " + email);
//                 return ResponseEntity.status(404).body(Map.of("error", "User not found"));
//             }

//             // itemCode를 itemDetailsId로 변환
//             Long itemIdLong = getItemDetailsIdByItemCode(itemId);
//             if (itemIdLong == null) {
//                 System.out.println("상품을 찾을 수 없음: " + itemId);
//                 return ResponseEntity.status(404).body(Map.of("error", "Item not found"));
//             }

//             // 위시리스트 토글
//             boolean isAdded = wishListService.toggleWishList(userId, itemIdLong);
            
//             System.out.println("위시리스트 토글 완료: " + (isAdded ? "추가됨" : "제거됨"));
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "isInWishList", isAdded,
//                 "message", isAdded ? "위시리스트에 추가되었습니다" : "위시리스트에서 제거되었습니다"
//             ));

//         } catch (Exception e) {
//             System.out.println("위시리스트 토글 실패: " + e.getMessage());
//             e.printStackTrace();
//             return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
//         }
//     }

//     /**
//      * 위시리스트에 상품 추가
//      */
//     @PostMapping("/wishlist/add/{itemId}")
//     public ResponseEntity<?> addToWishList(@PathVariable String itemId, HttpServletRequest request) {
//         System.out.println("=== 위시리스트 추가 API 요청 ===");
//         System.out.println("상품 ID: " + itemId);

//         try {
//             String email = getEmailFromToken(request);
//             if (email == null) {
//                 return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
//             }

//             Long userId = getUserIdByEmail(email);
//             if (userId == null) {
//                 return ResponseEntity.status(404).body(Map.of("error", "User not found"));
//             }

//             Long itemIdLong = getItemDetailsIdByItemCode(itemId);
//             if (itemIdLong == null) {
//                 return ResponseEntity.status(404).body(Map.of("error", "Item not found"));
//             }

//             WishList wishList = wishListService.addToWishList(userId, itemIdLong);
            
//             System.out.println("위시리스트 추가 완료");
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "message", "위시리스트에 추가되었습니다",
//                 "wishListId", wishList.getId()
//             ));

//         } catch (Exception e) {
//             System.out.println("위시리스트 추가 실패: " + e.getMessage());
//             e.printStackTrace();
//             return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
//         }
//     }

//     /**
//      * 위시리스트에서 상품 제거
//      */
//     @DeleteMapping("/wishlist/remove/{itemId}")
//     public ResponseEntity<?> removeFromWishList(@PathVariable String itemId, HttpServletRequest request) {
//         System.out.println("=== 위시리스트 제거 API 요청 ===");
//         System.out.println("상품 ID: " + itemId);

//         try {
//             String email = getEmailFromToken(request);
//             if (email == null) {
//                 return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
//             }

//             Long userId = getUserIdByEmail(email);
//             if (userId == null) {
//                 return ResponseEntity.status(404).body(Map.of("error", "User not found"));
//             }

//             Long itemIdLong = getItemDetailsIdByItemCode(itemId);
//             if (itemIdLong == null) {
//                 return ResponseEntity.status(404).body(Map.of("error", "Item not found"));
//             }

//             wishListService.removeFromWishList(userId, itemIdLong);
            
//             System.out.println("위시리스트 제거 완료");
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "message", "위시리스트에서 제거되었습니다"
//             ));

//         } catch (Exception e) {
//             System.out.println("위시리스트 제거 실패: " + e.getMessage());
//             e.printStackTrace();
//             return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
//         }
//     }

//     /**
//      * 사용자의 위시리스트 조회 (페이징)
//      */
//     @GetMapping("/wishlist")
//     public ResponseEntity<?> getUserWishList(
//             @RequestParam(defaultValue = "0") int page,
//             @RequestParam(defaultValue = "10") int size,
//             HttpServletRequest request) {
//         System.out.println("=== 위시리스트 조회 API 요청 ===");
//         System.out.println("페이지: " + page + ", 크기: " + size);

//         try {
//             String email = getEmailFromToken(request);
//             if (email == null) {
//                 return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
//             }

//             Long userId = getUserIdByEmail(email);
//             if (userId == null) {
//                 return ResponseEntity.status(404).body(Map.of("error", "User not found"));
//             }

//             Pageable pageable = PageRequest.of(page, size);
//             Page<WishList> wishList = wishListService.getUserWishList(userId, pageable);
            
//             System.out.println("위시리스트 조회 완료: " + wishList.getTotalElements() + "개 상품");
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "wishList", wishList.getContent(),
//                 "totalElements", wishList.getTotalElements(),
//                 "totalPages", wishList.getTotalPages(),
//                 "currentPage", wishList.getNumber()
//             ));

//         } catch (Exception e) {
//             System.out.println("위시리스트 조회 실패: " + e.getMessage());
//             e.printStackTrace();
//             return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
//         }
//     }

//     /**
//      * 사용자의 위시리스트 전체 조회
//      */
//     @GetMapping("/wishlist/all")
//     public ResponseEntity<?> getUserWishListAll(HttpServletRequest request) {
//         System.out.println("=== 위시리스트 전체 조회 API 요청 ===");

//         try {
//             String email = getEmailFromToken(request);
//             if (email == null) {
//                 return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
//             }

//             Long userId = getUserIdByEmail(email);
//             if (userId == null) {
//                 return ResponseEntity.status(404).body(Map.of("error", "User not found"));
//             }

//             List<WishList> wishList = wishListService.getUserWishListAll(userId);
            
//             System.out.println("위시리스트 전체 조회 완료: " + wishList.size() + "개 상품");
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "wishList", wishList,
//                 "totalCount", wishList.size()
//             ));

//         } catch (Exception e) {
//             System.out.println("위시리스트 전체 조회 실패: " + e.getMessage());
//             e.printStackTrace();
//             return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
//         }
//     }

//     /**
//      * 특정 상품이 위시리스트에 있는지 확인
//      */
//     @GetMapping("/wishlist/check/{itemId}")
//     public ResponseEntity<?> checkWishList(@PathVariable String itemId, HttpServletRequest request) {
//         System.out.println("=== 위시리스트 확인 API 요청 ===");
//         System.out.println("상품 ID: " + itemId);

//         try {
//             String email = getEmailFromToken(request);
//             if (email == null) {
//                 return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
//             }

//             Long userId = getUserIdByEmail(email);
//             if (userId == null) {
//                 return ResponseEntity.status(404).body(Map.of("error", "User not found"));
//             }

//             Long itemIdLong = getItemDetailsIdByItemCode(itemId);
//             if (itemIdLong == null) {
//                 return ResponseEntity.status(404).body(Map.of("error", "Item not found"));
//             }

//             boolean isInWishList = wishListService.isInWishList(userId, itemIdLong);
            
//             System.out.println("위시리스트 확인 완료: " + isInWishList);
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "isInWishList", isInWishList
//             ));

//         } catch (Exception e) {
//             System.out.println("위시리스트 확인 실패: " + e.getMessage());
//             e.printStackTrace();
//             return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
//         }
//     }

//     /**
//      * JWT 토큰에서 이메일 추출
//      */
//     private String getEmailFromToken(HttpServletRequest request) {
//         String authHeader = request.getHeader("Authorization");
//         if (authHeader != null && authHeader.startsWith("Bearer ")) {
//             try {
//                 String token = authHeader.substring(7);
//                 return jwtProvider.getSubject(token);
//             } catch (Exception e) {
//                 System.out.println("JWT 토큰 파싱 실패: " + e.getMessage());
//                 return null;
//             }
//         }
//         return null;
//     }

//     /**
//      * 이메일로 사용자 ID 조회
//      */
//     private Long getUserIdByEmail(String email) {
//         try {
//             Users user = usersRepository.findByEmail(email)
//                     .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
//             return user.getId();
//         } catch (Exception e) {
//             System.out.println("사용자 ID 조회 실패: " + e.getMessage());
//             return null;
//         }
//     }

//     /**
//      * itemCode로 ItemDetails의 itemId를 찾기
//      */
//     private Long getItemDetailsIdByItemCode(String itemCode) {
//         try {
//             System.out.println("=== 상품 조회 시작 ===");
//             System.out.println("조회할 itemCode: " + itemCode);
            
//             Item item = itemRepository.findByItemCode(itemCode)
//                     .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + itemCode));
            
//             System.out.println("Item 찾음: " + item.getName());
//             System.out.println("ItemDetails 개수: " + (item.getItemDetails() != null ? item.getItemDetails().size() : 0));
            
//             // Item의 첫 번째 ItemDetails를 사용 (일반적으로 하나의 Item은 하나의 ItemDetails를 가짐)
//             if (item.getItemDetails() != null && !item.getItemDetails().isEmpty()) {
//                 Long itemDetailsId = item.getItemDetails().get(0).getItemId();
//                 System.out.println("ItemDetails ID: " + itemDetailsId);
//                 return itemDetailsId;
//             } else {
//                 System.out.println("ItemDetails가 비어있음");
//                 throw new IllegalArgumentException("상품 상세 정보를 찾을 수 없습니다: " + itemCode);
//             }
//         } catch (Exception e) {
//             System.out.println("상품 상세 ID 조회 실패: " + e.getMessage());
//             e.printStackTrace();
//             return null;
//         }
//     }
// }
