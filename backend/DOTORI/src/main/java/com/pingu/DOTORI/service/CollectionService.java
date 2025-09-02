package com.pingu.DOTORI.service;

import com.pingu.DOTORI.entity.Collection;
import com.pingu.DOTORI.entity.CollectionMapping;
import com.pingu.DOTORI.repository.CollectionMappingRepository;
import com.pingu.DOTORI.repository.CollectionRepository;
import com.pingu.DOTORI.repository.DexItemProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionMappingRepository mappingRepository;

    /**
     * 구매 자동반영: (userId, itemCode)를 근거로 도감 생성/업서트
     * - 사용자 도감이 없으면 생성(Status=1 등 정책에 맞게)
     * - 매핑이 없으면 새로 생성, 있으면 값 업데이트
     */
    @Transactional
    public void applyPurchaseToCollection(Long userId, String itemCode) {
        // 1) 유저 도감 확보(없으면 생성)
        Collection col = collectionRepository.findFirstByUserId(userId)
                .orElseGet(() -> collectionRepository.save(
                        Collection.builder()
                                .userId(userId)
                                .status((byte) 1) // 활성(미인증) 등 정책 수치
                                .build()
                ));

        // 2) 매핑 업서트
        CollectionMapping mapping = mappingRepository
                .findByCollectionIdAndItemCode(col.getCollectionId(), itemCode)
                .orElseGet(() -> CollectionMapping.builder()
                        .collectionId(col.getCollectionId())
                        .itemCode(itemCode)
                        .status(CollectionMapping.OwnStatus.OWNED_UNVERIFIED)
                        .isOwn(1)
                        .isBuy(1)
                        .isCertified(0)
                        .verified(0)
                        .activatedAt(LocalDateTime.now())
                        .build()
                );

        // 구매 반영 시 보장하고 싶은 필드 갱신
        mapping.setIsBuy(1);
        mapping.setIsOwn(1);
        if (mapping.getActivatedAt() == null) {
            mapping.setActivatedAt(LocalDateTime.now());
        }

        mappingRepository.save(mapping);
    }

    /**
     * 사용자 도감 전체(매핑) 조회
     */
    @Transactional(readOnly = true)
    public List<CollectionMapping> getMyMappings(Long userId) {
        return collectionRepository.findFirstByUserId(userId)
                .map(c -> mappingRepository.findByCollectionId(c.getCollectionId()))
                .orElse(Collections.emptyList());
    }

    /**
     * 테스트 코드 인증 처리
     * - 테스트 코드를 검증하고 해당하는 아이템을 도감에 인증 상태로 추가
     */
    @Transactional
    public String verifyTestCode(Long userId, String code) {
        String itemKey = validateAndGetItemKey(code);
        String itemCode = convertDexKeyToItemCode(itemKey);
        
        // 1) 유저 도감 확보(없으면 생성)
        Collection col = collectionRepository.findFirstByUserId(userId)
                .orElseGet(() -> collectionRepository.save(
                        Collection.builder()
                                .userId(userId)
                                .status((byte) 2) // 활성-인증 상태
                                .build()
                ));

        // 2) 매핑 업서트 (인증 상태로)
        CollectionMapping mapping = mappingRepository
                .findByCollectionIdAndItemCode(col.getCollectionId(), itemCode)
                .orElseGet(() -> CollectionMapping.builder()
                        .collectionId(col.getCollectionId())
                        .itemCode(itemCode)
                        .status(CollectionMapping.OwnStatus.OWNED_VERIFIED)
                        .isOwn(1)
                        .isBuy(0) // 테스트 코드는 구매가 아님
                        .isCertified(1)
                        .verified(1)
                        .activatedAt(LocalDateTime.now())
                        .build()
                );

        // 인증 상태로 업데이트
        mapping.setStatus(CollectionMapping.OwnStatus.OWNED_VERIFIED);
        mapping.setIsOwn(1);
        mapping.setIsCertified(1);
        mapping.setVerified(1);
        if (mapping.getActivatedAt() == null) {
            mapping.setActivatedAt(LocalDateTime.now());
        }

        mappingRepository.save(mapping);
        return itemKey;
    }

    /**
     * 테스트 코드 검증 및 아이템 키 반환
     */
    private String validateAndGetItemKey(String code) {
        // 고정 더미 맵
        if ("TEST1".equals(code)) return "game:pokemon:001";
        if ("TEST2".equals(code)) return "game:pokemon:002";
        if ("TEST3".equals(code)) return "game:pokemon:003";
        if ("TEST4".equals(code)) return "game:pokemon:004";
        if ("TEST5".equals(code)) return "game:pokemon:005";
        if ("TEST6".equals(code)) return "game:pokemon:006";
        if ("OP001".equals(code)) return "animation:onepiece:001";
        if ("OP002".equals(code)) return "animation:onepiece:002";
        if ("OP003".equals(code)) return "animation:onepiece:003";

        // 패턴 매칭 (POKEMON/PKM/KIMETSU/KIM/ONEPIECE/OP)
        if (code.matches("^(POKEMON|PKM)[-_]?(\\d{1,3})$")) {
            String[] parts = code.split("[-_]?(?=\\d)");
            int n = Integer.parseInt(parts[1]);
            if (n >= 1 && n <= 151) { // 포켓몬 1세대 범위
                return String.format("game:pokemon:%03d", n);
            }
        }
        
        if (code.matches("^(KIMETSU|KIM)[-_]?(\\d{1,3})$")) {
            String[] parts = code.split("[-_]?(?=\\d)");
            int n = Integer.parseInt(parts[1]);
            if (n >= 1 && n <= 50) { // 귀멸의 칼날 범위
                return String.format("animation:kimetsu:%03d", n);
            }
        }
        
        if (code.matches("^(ONEPIECE|OP)[-_]?(\\d{1,3})$")) {
            String[] parts = code.split("[-_]?(?=\\d)");
            int n = Integer.parseInt(parts[1]);
            if (n >= 1 && n <= 30) { // 원피스 범위
                return String.format("animation:onepiece:%03d", n);
            }
        }

        throw new IllegalArgumentException("유효하지 않은 테스트 코드입니다: " + code);
    }

    /**
     * DEX 키를 DB 아이템 코드로 변환
     */
    private String convertDexKeyToItemCode(String dexKey) {
        // dexKey 형식: "game:pokemon:001" -> 적절한 itemCode로 변환
        // 현재는 단순히 dexKey를 그대로 사용하지만, 필요에 따라 변환 로직 추가 가능
        return dexKey;
    }

    /**
     * 프론트엔드 DEX 형식에 맞는 상태 반환
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDexState(Long userId) {
        Optional<Collection> colOpt = collectionRepository.findFirstByUserId(userId);
        List<Map<String, Object>> items = new ArrayList<>();
        if (colOpt.isPresent()) {
            Long colId = colOpt.get().getCollectionId();
            List<DexItemProjection> rows = mappingRepository.findDexItemsByCollectionId(colId);
            for (DexItemProjection r : rows) {
                String itemCode = r.getItemCode();
                if (itemCode == null || itemCode.isBlank()) continue;
                boolean activated = Integer.valueOf(1).equals(r.getIsOwn());
                boolean verified = Integer.valueOf(1).equals(r.getIsCertified());
                Map<String, Object> item = new HashMap<>();
                item.put("itemKey", itemCode);
                item.put("activated", activated);
                item.put("verified", verified);
                items.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", "user-" + userId); // 간단한 userId 형식
        result.put("items", items);
        
        return result;
    }

    /**
     * 아이템 활성화/비활성화 토글
     */
    @Transactional
    public void toggleItemActivation(Long userId, String itemKey, Boolean activated) {
        String itemCode = convertDexKeyToItemCode(itemKey);
        
        // 1) 유저 도감 확보(없으면 생성)
        Collection col = collectionRepository.findFirstByUserId(userId)
                .orElseGet(() -> collectionRepository.save(
                        Collection.builder()
                                .userId(userId)
                                .status((byte) 1) // 활성 상태
                                .build()
                ));

        // 2) 매핑 찾기 또는 생성
        CollectionMapping mapping = mappingRepository
                .findByCollectionIdAndItemCode(col.getCollectionId(), itemCode)
                .orElseGet(() -> CollectionMapping.builder()
                        .collectionId(col.getCollectionId())
                        .itemCode(itemCode)
                        .status(CollectionMapping.OwnStatus.INACTIVE)
                        .isOwn(0)
                        .isBuy(0)
                        .isCertified(0)
                        .verified(0)
                        .build()
                );

        // 3) 인증된 항목은 비활성화할 수 없음
        if (mapping.getIsCertified() == 1 && !activated) {
            return; // 인증된 항목은 비활성화 불가
        }

        // 4) 활성화 상태 업데이트
        mapping.setIsOwn(activated ? 1 : 0);
        if (activated && mapping.getActivatedAt() == null) {
            mapping.setActivatedAt(LocalDateTime.now());
        }
        if (!activated) {
            mapping.setDeactivatedAt(LocalDateTime.now());
        }

        mappingRepository.save(mapping);
    }
}
