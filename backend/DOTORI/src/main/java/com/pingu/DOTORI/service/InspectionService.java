package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.AdminListRow;
import com.pingu.DOTORI.dto.InspectionRequest;
import com.pingu.DOTORI.entity.Admin;
import com.pingu.DOTORI.entity.ItemDetails;
import com.pingu.DOTORI.entity.ItemImg;
import com.pingu.DOTORI.repository.AdminRepository;
import com.pingu.DOTORI.repository.ItemDetailsRepository;
import com.pingu.DOTORI.repository.ItemImgRepository;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import com.pingu.DOTORI.util.ImageMetadataUtil;

@Service
public class InspectionService {

    private final AdminRepository adminRepository;
    private final ItemDetailsRepository itemDetailsRepository;
    private final ItemImgRepository itemImgRepository;

    public InspectionService(AdminRepository adminRepository,
                             ItemDetailsRepository itemDetailsRepository,
                             ItemImgRepository itemImgRepository) {
        this.adminRepository = adminRepository;
        this.itemDetailsRepository = itemDetailsRepository;
        this.itemImgRepository = itemImgRepository;
    }

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
        // 1. ItemDetails 조회
    	ItemDetails itemDetails = itemDetailsRepository
    		    .findFirstByItem_ItemCodeAndUser_Id(dto.getItemCode(), dto.getUserId())
    		    .orElseThrow(() -> new IllegalArgumentException(
    		        "잘못된 아이템 코드 또는 유저 ID: " + dto.getItemCode() + ", user=" + dto.getUserId()
    		    ));

        // 2. Admin 엔티티 생성
        Admin admin = Admin.builder()
                .itemDetails(itemDetails)
                .registrationDate(LocalDateTime.now())
                .admissionState(0) // 0 = 대기
                .quality(null)     // 등급은 관리자 승인 때 업데이트
                .itemExplanation(dto.getMemo())
                .build();

        adminRepository.save(admin);

        // 3. 이미지 저장
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

            Long imageCount;
            if (row[10] instanceof java.math.BigInteger bi) {
                imageCount = bi.longValue();
            } else if (row[10] instanceof Long l) {
                imageCount = l;
            } else if (row[10] instanceof Integer iVal) {
                imageCount = iVal.longValue();
            } else {
                imageCount = 0L;
            }

            Long inspectionId;
            Object id0 = row[0];
            if (id0 instanceof java.math.BigInteger bi0) inspectionId = bi0.longValue();
            else if (id0 instanceof Long l0) inspectionId = l0;
            else if (id0 instanceof Integer i0) inspectionId = i0.longValue();
            else inspectionId = 0L;

            Long itemId;
            Object id1 = row[1];
            if (id1 instanceof java.math.BigInteger bi1) itemId = bi1.longValue();
            else if (id1 instanceof Long l1) itemId = l1;
            else if (id1 instanceof Integer i1) itemId = i1.longValue();
            else itemId = 0L;

            return new AdminListRow(
                    Long.valueOf(inspectionId),
                    Long.valueOf(itemId),
                    (String) row[2],
                    (String) row[3],
                    (BigDecimal) row[4],
                    convertToBoolean(row[5]),
                    convertToLocalDateTime(row[6]),
                    (Integer) row[7],
                    (Integer) row[8],
                    (String) row[9],
                    Long.valueOf(imageCount),
                    convertToLocalDateTime(row[11]),
                    imageUrls
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

            Long imageCount;
            if (row[10] instanceof java.math.BigInteger bi) {
                imageCount = bi.longValue();
            } else if (row[10] instanceof Long l) {
                imageCount = l;
            } else if (row[10] instanceof Integer iVal) {
                imageCount = iVal.longValue();
            } else {
                imageCount = 0L;
            }

            Long inspectionId;
            Object id0 = row[0];
            if (id0 instanceof java.math.BigInteger bi0) inspectionId = bi0.longValue();
            else if (id0 instanceof Long l0) inspectionId = l0;
            else if (id0 instanceof Integer i0) inspectionId = i0.longValue();
            else inspectionId = 0L;

            Long itemId;
            Object id1 = row[1];
            if (id1 instanceof java.math.BigInteger bi1) itemId = bi1.longValue();
            else if (id1 instanceof Long l1) itemId = l1;
            else if (id1 instanceof Integer i1) itemId = i1.longValue();
            else itemId = 0L;

            return new AdminListRow(
                    Long.valueOf(inspectionId),
                    Long.valueOf(itemId),
                    (String) row[2],
                    (String) row[3],
                    (BigDecimal) row[4],
                    convertToBoolean(row[5]),
                    convertToLocalDateTime(row[6]),
                    (Integer) row[7],
                    (Integer) row[8],
                    (String) row[9],
                    Long.valueOf(imageCount),
                    convertToLocalDateTime(row[11]),
                    imageUrls
            );
        });
    }

    private LocalDateTime convertToLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (value instanceof java.util.Date d) {
            return Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        if (value instanceof CharSequence cs) {
            String s = cs.toString();
            if (s.contains(" ") && !s.contains("T")) s = s.replace(' ', 'T');
            try { return LocalDateTime.parse(s); } catch (Exception ignored) {}
        }
        return null;
    }

    private Boolean convertToBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        if (value instanceof CharSequence cs) {
            String s = cs.toString();
            return "1".equals(s) || Boolean.parseBoolean(s);
        }
        return false;
    }

    @Transactional
    public void approve(Long inspectionId, Integer grade, String reason) {
        Admin admin = adminRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("검수 ID를 찾을 수 없음: " + inspectionId));

        admin.setAdmissionState(1); // 1 = 승인
        admin.setQuality(grade);
        adminRepository.save(admin);
    }

    @Transactional
    public void reject(Long inspectionId, Integer grade, String reason) {
        Admin admin = adminRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("검수 ID를 찾을 수 없음: " + inspectionId));

        admin.setAdmissionState(2); // 2 = 반려
        admin.setQuality(grade);
        admin.setItemExplanation(reason);
        adminRepository.save(admin);
    }
}
