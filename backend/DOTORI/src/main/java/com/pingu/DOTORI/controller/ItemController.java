package com.pingu.DOTORI.controller;


<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
=======
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
>>>>>>> 051fab3ea31a8c59d0f6ae6bbb7a5475fd854e22
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pingu.DOTORI.dto.ItemDTO;
import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.service.ItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
	private final ItemService itemService;
	
	@GetMapping
<<<<<<< HEAD
	public Page<Item> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
		return itemService.findAll(PageRequest.of(page, size));
=======
	public ResponseEntity<Page<ItemDTO>> getItems(@PageableDefault(size = 20) Pageable pageable){
		return ResponseEntity.ok(itemService.findAll(pageable));
>>>>>>> 051fab3ea31a8c59d0f6ae6bbb7a5475fd854e22
	}
}
