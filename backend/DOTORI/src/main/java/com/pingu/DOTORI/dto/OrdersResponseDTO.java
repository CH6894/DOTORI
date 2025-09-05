package com.pingu.DOTORI.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class OrdersResponseDTO {
    private Long orderId;          // 주문 PK
    private String itemName;       // 상품명
    private String title;          // 상품 타이틀
    private String mainImageUrl;   // 상품 대표 이미지
    private int quantity;          // 수량
    private Long price;            // 단가
    private LocalDateTime payTime; // 결제 시각
    private String payMethod;      // 결제 수단
    private String depositerName;  // 입금자명 (추가)
    private String payMessage;     // 배송 요청사항 (추가)
}
