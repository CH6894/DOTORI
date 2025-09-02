package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.entity.CollectionMapping;
import com.pingu.DOTORI.service.CollectionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * NOTE: 세션에서 userId를 읽는 기존 컨벤션에 맞춤.
 * 실제 로그인 방식(JWT 등)으로 가져오면 교체.
 */
@RestController
@RequestMapping("/api/collection")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    /**
     * 테스트용 세션 설정 API
     * - 개발 환경에서 userId 세션을 설정하기 위한 임시 엔드포인트
     */
    @PostMapping("/test/set-session")
    public ResponseEntity<?> setTestSession(@RequestBody Map<String, Object> request, HttpSession session) {
        Object userIdObj = request.get("userId");
        Long userId = null;
        
        if (userIdObj instanceof Number) {
            userId = ((Number) userIdObj).longValue();
        } else if (userIdObj instanceof String) {
            try {
                userId = Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "유효하지 않은 userId입니다"));
            }
        }
        
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "userId가 필요합니다"));
        }
        
        session.setAttribute("userId", userId);
        return ResponseEntity.ok(Map.of("ok", true, "message", "세션이 설정되었습니다", "userId", userId));
    }

    /** 내 도감(매핑) 조회 */
    @GetMapping("/me")
    public ResponseEntity<List<CollectionMapping>> myCollection(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(collectionService.getMyMappings(userId));
    }

    /** DEX 상태 조회 API - 프론트엔드 형식에 맞게 변환 */
    @GetMapping("/dex-state")
    public ResponseEntity<?> getDexState(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        
        try {
            Map<String, Object> dexState = collectionService.getDexState(userId);
            return ResponseEntity.ok(dexState);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "서버 오류가 발생했습니다"));
        }
    }

    /**
     * (테스트/운영툴용) 구매 반영 API
     * - 프론트/결제 훅이 안정되기 전까지 수동 호출로 점검 가능
     */
    @PostMapping("/apply-purchase")
    public ResponseEntity<Void> applyPurchase(@RequestParam String itemCode, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        collectionService.applyPurchaseToCollection(userId, itemCode);
        return ResponseEntity.ok().build();
    }

    /**
     * 테스트 코드 인증 API
     * - 프론트엔드 DEX에서 테스트 코드 입력 시 인증 처리
     */
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        
        String code = request.get("code");
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "코드가 필요합니다"));
        }
        
        try {
            String itemKey = collectionService.verifyTestCode(userId, code.trim().toUpperCase());
            return ResponseEntity.ok(Map.of("ok", true, "itemKey", itemKey));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", e.getMessage()));
        }
    }

    /**
     * 아이템 활성화/비활성화 API
     */
    @PostMapping("/activate")
    public ResponseEntity<?> activateItem(@RequestBody Map<String, Object> request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        
        String itemKey = (String) request.get("itemKey");
        Boolean activated = (Boolean) request.getOrDefault("activated", true);
        
        if (itemKey == null || itemKey.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "itemKey가 필요합니다"));
        }
        
        try {
            collectionService.toggleItemActivation(userId, itemKey, activated);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("ok", false, "message", "서버 오류가 발생했습니다"));
        }
    }
}
