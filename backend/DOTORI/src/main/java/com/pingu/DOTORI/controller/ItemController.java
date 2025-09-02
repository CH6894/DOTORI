package com.pingu.DOTORI.controller;

import java.util.List;
import java.util.Optional;

import org.aspectj.apache.bcel.classfile.Module.Require;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pingu.DOTORI.dto.ItemDTO;
import com.pingu.DOTORI.dto.ItemDetailsDTO;
import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.entity.ItemDetails;
import com.pingu.DOTORI.repository.ItemRepository;
import com.pingu.DOTORI.service.ItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/open/items")
@RequiredArgsConstructor
public class ItemController {
	private final ItemService itemService;

	@GetMapping
	public ResponseEntity<Page<ItemDTO>> findAll(@PageableDefault(size = 18) Pageable pageable) {
		return ResponseEntity.ok(itemService.findAll(pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ItemDTO> findById(@PathVariable String id) {
		return ResponseEntity.ok(itemService.findById(id));
	}

	@GetMapping("/genre/{genre}")
	public ResponseEntity<Page<ItemDTO>> byGenre(@PathVariable String genre,
			@PageableDefault(size = 300) Pageable pageable) {
		return ResponseEntity.ok(itemService.findByGenre(genre, pageable));
	}

	@GetMapping("/genre/{genre}/title/{title}")
	public ResponseEntity<Page<ItemDTO>> byGenreAndTitle(@PathVariable String genre,
			@PathVariable(required = false) String title, @PageableDefault(size = 300) Pageable pageable) {
		return ResponseEntity.ok(itemService.findByGenreAndTitle(genre, title, pageable));
	}

	@GetMapping("/{itemCode}/approved-unpacked-details")
	public ResponseEntity<List<ItemDetailsDTO>> getApprovedUnpackedItemDetails(@PathVariable String itemCode) {
		List<ItemDetailsDTO> details = itemService.findApprovedUnpackedItemDetailsByItemCodeAsDTO(itemCode);
		return ResponseEntity.ok(details);
	}
	
	@GetMapping("/{itemCode}/approved-opened-details")
	public ResponseEntity<List<ItemDetailsDTO>> getApprovedOpenedItemDetails(@PathVariable String itemCode) {
		List<ItemDetailsDTO> details = itemService.findApprovedOpenedItemDetailsByItemCode(itemCode);
		return ResponseEntity.ok(details);
	}

}
