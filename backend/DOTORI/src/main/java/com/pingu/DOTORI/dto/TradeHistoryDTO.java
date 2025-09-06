package com.pingu.DOTORI.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TradeHistoryDTO {
    private String no;              // 거래번호 (O + orderId 또는 S + itemDetailsId)
    private String kind;            // 거래유형 (buy/sell)
    private String item;            // 상품명
    private Long price;             // 가격
    private TradeState state;       // 거래상태
    private LocalDateTime date;     // 거래일시
    private String image;           // 상품 이미지 URL
    
    @Getter
    @Setter
    @Builder
    public static class TradeState {
        private String type;        // 상태 타입 (buy-done, sell-done, selling)
        private String text;        // 상태 텍스트 (구매 완료, 판매 완료, 판매중)
    }
}
