package com.pingu.DOTORI.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDetailsDTO {
	private Long itemId;
	private BigDecimal cost;
	private Boolean status;
	private Boolean unpacked;
	private String productCondition;

	// Admin 정보 (개봉 상품용)
	private Integer quality; // 등급 (1=S, 2=A, 3=B, 4=C)
	private LocalDateTime registrationDate; // 판매등록일
	private String itemExplanation; // 판매자 코멘트

	// Item 정보
	private String itemCode;
	private String itemName;
	private String itemImgUrl;
}
