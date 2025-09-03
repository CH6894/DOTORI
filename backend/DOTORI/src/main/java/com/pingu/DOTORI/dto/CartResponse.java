package com.pingu.DOTORI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long cartId; // 장바구니 ID
    private Long itemDetailsId; // 상품 상세 ID
    private String itemName; // 상품 이름 (ItemDetails → Item에서 가져올 수 있음)
    private int quantity; // 수량
    private String thumbnailUrl; // 상품 이미지 (대표 이미지 1개)
    private int price; // 가격 (예: cost)
}