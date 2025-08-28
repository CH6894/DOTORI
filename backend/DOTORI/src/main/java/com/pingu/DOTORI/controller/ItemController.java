package com.pingu.DOTORI.controller;


import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.repository.ItemRepository;
import com.pingu.DOTORI.service.ItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/open/items")
@RequiredArgsConstructor
public class ItemController {
	private final ItemService itemService;
	
	@GetMapping
	public List<Item> getAllItems(){
		return itemService.findAll();
	}
	
	@GetMapping("/{itemCode}")
	public Item getItem(@PathVariable String itemCode) {
		return itemService.findOne(itemCode);
	}
}
