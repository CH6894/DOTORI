package com.pingu.DOTORI.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrdersRequestDTO {
	

    // ✅ 장바구니 기반 주문일 경우
    private List<Long> cartIds;

    // ✅ 바로구매(단일 상품) 주문일 경우
    private Long itemDetailsId;

    // 공통 필드
    private String depositerName;   // 입금자명
    private String payMethod;       // 결제 방법 (bank, card 등)
    private String payMessage;      // 배송 요청사항
}
