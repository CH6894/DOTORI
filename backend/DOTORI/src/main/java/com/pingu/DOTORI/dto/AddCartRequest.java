package com.pingu.DOTORI.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCartRequest {
	private Long itemDetailsId; // 프론트에서 보낼 상품 상세 ID
}
