package com.pingu.DOTORI.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishListItemDto {
    private Long wishlistId;
    private Long itemId;
    private String name;
    private String itemImg;
    private String cost;
}


