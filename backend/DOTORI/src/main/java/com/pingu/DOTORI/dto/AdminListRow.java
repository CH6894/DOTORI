package com.pingu.DOTORI.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class AdminListRow {
    private Long inspectionId;
    private Long itemId;
    private String title;
    private String sellerName;
    private BigDecimal cost;
    private Boolean unpacked;
    private LocalDateTime registrationDate;
    private Integer admissionState;
    private Integer quality;
    private String itemExplanation;
    private Long imageCount;
    private LocalDateTime filmingTime;
    private List<String> imageUrls;

    // ✅ JPQL과 100% 매칭되는 생성자
    public AdminListRow(Long inspectionId,
            Long itemId,
            String title,
            String userName,
            BigDecimal cost,
            Boolean unpacked,
            LocalDateTime registrationDate,
            Integer admissionState,
            Integer quality,
            String itemExplanation,
            Long imageCount,
            LocalDateTime filmingTime,
            List<String> imageUrls) {
        this.inspectionId = inspectionId;
        this.itemId = itemId;
        this.title = title;
        this.sellerName = userName; // userName → sellerName 매핑
        this.cost = cost;
        this.unpacked = unpacked;
        this.registrationDate = registrationDate;
        this.admissionState = admissionState;
        this.quality = quality;
        this.itemExplanation = itemExplanation;
        this.imageCount = imageCount;
        this.filmingTime = filmingTime;
        this.imageUrls = imageUrls;
    }

    // 제거된 오버로드 생성자: 오토박싱으로 인해 모호성 유발
}
