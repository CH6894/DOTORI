package com.pingu.DOTORI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// 주문 요청 DTO- 장바구니 → 주문 생성할 때 사용

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    private List<Long> cartIds; // 주문할 장바구니 ID 목록
    private String depositerName; // 입금자명
    private String payMessage; // 배송 요청사항
}
