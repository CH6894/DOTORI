package com.pingu.DOTORI.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pingu.DOTORI.dto.PriceDTO;
import com.pingu.DOTORI.service.PriceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/open/items")
@RequiredArgsConstructor
public class PriceController {
	private final PriceService priceService;

	@GetMapping("/{itemId}/price")
	public List<PriceDTO> getPrice(
			@PathVariable Long itemId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		return priceService.getPrice(itemId, from, to);
	}

	/* 기간별 시세 이력 조회 */
	@GetMapping("/{id}/price-history/{period}")
	public ResponseEntity<List<PriceDTO>> getPriceHistoryByPeriod(
			@PathVariable Long id,
			@PathVariable String period) {

		List<PriceDTO> priceHistory = priceService.getPriceHistoryByPeriod(id, period);
		return ResponseEntity.ok(priceHistory);
	}

}
