package com.pingu.DOTORI.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pingu.DOTORI.dto.ItemDTO;
import com.pingu.DOTORI.dto.PageResponse;
import com.pingu.DOTORI.service.ItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
	
	private final ItemService itemService;
	
	@GetMapping
	public PageResponse<ItemDTO> list(
				@RequestParam String genre,
				@RequestParam(required = false) String manufacturer, 
				@RequestParam(defaultValue = "0") int page, 
				@RequestParam(defaultValue = "20") int size
	)	{
		return itemService.getItems(genre, manufacturer, page, size);
	}
}
