package com.pingu.DOTORI.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * 판매자 검수 신청을 받을 DTO
 */
@Data
public class InspectionRequest {
    private Long userId;              // 사용자 ID
    private String itemCode;          // 상품 코드 (ex: DT-ADP-0001)
    private String productTitle;      // 상품명
    private BigDecimal price;         // 가격
    private int unpacked;             // 개봉 여부 (0=미개봉, 1=개봉)
    private String memo;              // 메모
    private List<MultipartFile> images; // 첨부 이미지
}
