package com.pingu.DOTORI.dto;


import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDTO {
	private String itemCode;
	private String name;
	private String title;
	private String manufacturer;
	private String material;
	private LocalDate releaseMonth;
	private String size;
	private String information;
	private String imgUrl;
	private Long storageFees;
	private String genre;
	private BigDecimal cost;
}
