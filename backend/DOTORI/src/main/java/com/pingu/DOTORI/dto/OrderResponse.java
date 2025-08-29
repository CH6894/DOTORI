package com.pingu.DOTORI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// 주문 내역 응답 DTO

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;          // 주문 ID
    private String itemName;       // 상품 이름
    private int price;             // 상품 가격
    private int quantity;          // 수량 (Cart → Orders 변환 시 기본 1)
    private String depositerName;  // 입금자명
    private String payMethod;      // 결제 방식 (무통장)
    private String payMessage;     // 배송 요청사항
    private LocalDateTime payTime; // 결제 시간
}
