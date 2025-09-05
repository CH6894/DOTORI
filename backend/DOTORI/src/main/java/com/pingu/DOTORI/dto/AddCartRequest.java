package com.pingu.DOTORI.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddCartRequest {
    private Long itemDetailsId; // item_details PK
    private int quantity;       // 장바구니 수량
}