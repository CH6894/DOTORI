package com.pingu.DOTORI.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class CartResponseDTO {
    private Long cartId;
    private String itemName;
    private String title;
    private String mainImageUrl;
    private int quantity;
    private Long price;
}
