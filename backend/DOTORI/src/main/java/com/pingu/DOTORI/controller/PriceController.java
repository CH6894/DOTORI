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

	// 아이템 아이디로만 찾아서 크게 필요 없을 것 같음.
	@GetMapping("/{itemId}/price")
	public List<PriceDTO> getPrice(
			@PathVariable Long itemId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		return priceService.getPrice(itemId, from, to);
	}

	/* 기간별 시세 이력 조회 */
	@GetMapping("/{id}/price-history/period")
	public ResponseEntity<List<PriceDTO>> getPriceHistoryByPeriod(
			@PathVariable Long itemId,
			@PathVariable String period) {

		List<PriceDTO> priceHistory = priceService.getPriceHistoryByPeriod(itemId, period);
		return ResponseEntity.ok(priceHistory);
	}
	// 여기까지.

	// 아이템 코드로 조회하기.
	@GetMapping("/code/{itemCode}/price-history/{period}")
	public List<PriceDTO> getPriceHistoryByCodePreset(
			@PathVariable String itemCode,
			@RequestParam(defaultValue = "1M") String period) {
		return priceService.getPriceByCode(itemCode, period);
	}

}
