package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.AdminListRow;
import com.pingu.DOTORI.dto.InspectionRequest;
import com.pingu.DOTORI.entity.Admin;
import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.entity.ItemDetails;
import com.pingu.DOTORI.entity.ItemImg;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.repository.AdminRepository;
import com.pingu.DOTORI.repository.ItemDetailsRepository;
import com.pingu.DOTORI.repository.ItemImgRepository;
import com.pingu.DOTORI.repository.ItemRepository;
import com.pingu.DOTORI.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import com.pingu.DOTORI.util.ImageMetadataUtil;

@Service
@RequiredArgsConstructor
public class InspectionService {

    private final AdminRepository adminRepository;
    private final ItemDetailsRepository itemDetailsRepository;
    private final ItemImgRepository itemImgRepository;
    private final ItemRepository itemRepository;
    private final UsersRepository usersRepository;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateResult {
        private Long inspectionId;
        private String message;
    }

    // ================== 검수 신청 생성 ==================
    @Transactional
    public CreateResult createInspection(InspectionRequest dto) throws IOException {
        // 1. Item 엔티티 조회
        Item item = itemRepository.findById(dto.getItemCode())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없음: " + dto.getItemCode()));
        
        // 2. User 엔티티 조회 (JWT에서 이메일 추출)
        final String userEmail;
        String emailFromDto = dto.getUserEmail();
        if (emailFromDto == null || emailFromDto.isEmpty()) {
            // SecurityContext에서 현재 인증된 사용자 이메일 가져오기
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                userEmail = ((org.springframework.security.core.userdetails.User) principal).getUsername();
            } else {
                throw new IllegalArgumentException("사용자 이메일을 찾을 수 없음");
            }
        } else {
            userEmail = emailFromDto;
        }
        
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없음: " + userEmail));

        // 3. ItemDetails 새로 생성
        ItemDetails itemDetails = ItemDetails.builder()
                .item(item)
                .user(user)
                .cost(dto.getPrice())
                .status(true) // 활성 상태
                .unpacked(dto.getUnpacked() == 1) // 0=미개봉, 1=개봉
                .storageDate(LocalDateTime.now())
                .build();
        
        itemDetailsRepository.save(itemDetails);

        // 4. Admin 엔티티 생성
        Admin admin = Admin.builder()
                .itemDetails(itemDetails)
                .registrationDate(LocalDateTime.now())
                .admissionState(0) // 0 = 대기
                .quality(null)     // 등급은 관리자 승인 때 업데이트
                .itemExplanation(dto.getMemo())
                .build();

        adminRepository.save(admin);

        // 5. 이미지 저장
        saveImages(dto.getImages(), itemDetails, dto.getFilmingTime());

        return new CreateResult(admin.getInspectionId(), "검수 신청이 완료되었습니다.");
    }

    // ================== 이미지 저장 ==================
    public void saveImages(List<MultipartFile> images, ItemDetails item, OffsetDateTime requestFilmingTime) {
        for (MultipartFile image : images) {
            if (image.isEmpty()) continue;

            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();

            try {
                Path savePath = Paths.get("C:/uploads/items/" + filename);
                Files.createDirectories(savePath.getParent());
                image.transferTo(savePath);
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패", e);
            }

            // EXIF 메타데이터에서 촬영시각 추출 → 없으면 requestFilmingTime 사용 → 그것도 없으면 현재시간
            LocalDateTime filmingTime = ImageMetadataUtil.extractFilmingTime(image);
            if (filmingTime == null && requestFilmingTime != null) {
                filmingTime = requestFilmingTime.toLocalDateTime();
            }
            if (filmingTime == null) {
                filmingTime = LocalDateTime.now();
            }

            ItemImg itemImg = ItemImg.builder()
                    .itemDetails(item)
                    .imgUrl(filename)
                    .imgType((byte) 0)
                    .filmingTime(filmingTime)
                    .build();

            itemImgRepository.save(itemImg);
        }
    }


    // ================== 상태 및 등급 업데이트 ==================
    @Transactional
    public void updateStatusAndGrade(Long inspectionId, Integer status, Integer grade) {
        Admin admin = adminRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("검수 ID를 찾을 수 없음: " + inspectionId));

        admin.setAdmissionState(status);
        admin.setQuality(grade);
        adminRepository.save(admin);
    }

    // ================== 관리자 검수 목록 조회 ==================
    public Page<AdminListRow> getAdminInspections(Integer state, String from, String to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        LocalDateTime fromDate = (from != null && !from.isBlank()) ? LocalDateTime.parse(from) : null;
        LocalDateTime toDate = (to != null && !to.isBlank()) ? LocalDateTime.parse(to) : null;

        Page<Object[]> results = adminRepository.findByFiltersNative(state, fromDate, toDate, pageable);

        return results.map(row -> {
            String baseUrl = "http://localhost:8081/uploads/items/";

            List<String> imageUrls = row[12] != null
                    ? Arrays.stream(row[12].toString().split(","))
                            .map(url -> baseUrl + url) // prefix 붙여줌
                            .toList()
                    : Collections.emptyList();

            return new AdminListRow(
                    safeLongValue(row[0]),               // inspectionId
                    safeLongValue(row[1]),               // itemId
                    (String) row[2],                     // title
                    (String) row[3],                     // sellerName
                    (BigDecimal) row[4],                 // cost
                    (Boolean) row[5],                    // unpacked
                    safeLocalDateTimeValue(row[6]),      // registrationDate
                    (Integer) row[7],                    // admissionState
                    (Integer) row[8],                    // quality
                    (String) row[9],                     // itemExplanation
                    safeLongValue(row[10]),              // imageCount
                    safeLocalDateTimeValue(row[11]),     // filmingTime
                    imageUrls                            // imageUrls
            );
        });
    }

    // ================== AdminInspectionController 전용 ==================
    public Page<AdminListRow> getFilteredAdminList(Integer state, LocalDateTime from, LocalDateTime to, PageRequest pageRequest) {
        Page<Object[]> results = adminRepository.findByFiltersNative(state, from, to, pageRequest);

        return results.map(row -> {
            String baseUrl = "http://localhost:8081/uploads/items/";

            List<String> imageUrls = row[12] != null
                    ? Arrays.stream(row[12].toString().split(","))
                            .map(url -> baseUrl + url)
                            .toList()
                    : Collections.emptyList();

            return new AdminListRow(
                    safeLongValue(row[0]),
                    safeLongValue(row[1]),
                    (String) row[2],
                    (String) row[3],
                    (BigDecimal) row[4],
                    (Boolean) row[5],
                    safeLocalDateTimeValue(row[6]),
                    (Integer) row[7],
                    (Integer) row[8],
                    (String) row[9],
                    safeLongValue(row[10]),
                    safeLocalDateTimeValue(row[11]),
                    imageUrls
            );
        });
    }

    @Transactional
    public void approve(Long inspectionId, Integer grade, String reason) {
        Admin admin = adminRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("검수 ID를 찾을 수 없음: " + inspectionId));

        admin.setAdmissionState(1); // 1 = 승인
        admin.setQuality(grade);
        admin.setRejectionReason(null); // 승인 시 반려사유는 null로 설정
        adminRepository.save(admin);
    }

    @Transactional
    public void reject(Long inspectionId, Integer grade, String reason) {
        Admin admin = adminRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("검수 ID를 찾을 수 없음: " + inspectionId));

        admin.setAdmissionState(2); // 2 = 반려
        admin.setQuality(null); // 반려 시 등급은 null로 설정
        admin.setRejectionReason(parseRejectionReason(reason)); // 반려사유를 숫자로 변환하여 저장
        adminRepository.save(admin);
    }

    // ================== 헬퍼 메서드 ==================
    private Long safeLongValue(Object value) {
        if (value == null) return 0L;
        if (value instanceof BigInteger) return ((BigInteger) value).longValue();
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        // String으로 들어온 경우도 처리
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }

    private LocalDateTime safeLocalDateTimeValue(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate().atStartOfDay();
        }
        if (value instanceof String) {
            try {
                return LocalDateTime.parse((String) value);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // 반려사유 문자열을 숫자로 변환하는 헬퍼 메서드
    private Integer parseRejectionReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return null;
        }
        
        // 반려사유 매핑 (프론트엔드의 defaultReasons 배열 순서와 일치)
        if (reason.contains("촬영 각도/장면 부족")) return 1;
        if (reason.contains("해상도/초점 문제")) return 2;
        if (reason.contains("라벨/시리얼 확인 불가")) return 3;
        if (reason.contains("상품 상태 설명 불충분")) return 4;
        if (reason.contains("광고/워터마크 포함")) return 5;
        
        // 기본값
        return 1;
    }

}
