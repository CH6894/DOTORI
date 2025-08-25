package com.pingu.DOTORI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDTO {
	private String ean;
	private String name;
	private String title;
	private String manufacturer;
	private String texture;
	private String size;
	private String imgUrl;
	private Long storageFees;
	private String genre;
}
