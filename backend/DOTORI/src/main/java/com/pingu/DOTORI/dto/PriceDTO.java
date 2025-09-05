package com.pingu.DOTORI.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PriceDTO {
	private Long itemId;
	private LocalDateTime payTime;
	private BigDecimal price;
}
