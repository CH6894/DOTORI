package com.pingu.DOTORI.service;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.pingu.DOTORI.dto.ItemDTO;
import com.pingu.DOTORI.dto.ItemMapper;
import com.pingu.DOTORI.dto.PageResponse;
import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {
	
	private final ItemRepository itemRepository;
	
	public PageResponse<ItemDTO> getItems(String genre, String manufacturer, int page, int size){
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ean"));
		
		Page<Item> result = (manufacturer == null || manufacturer.isBlank())
				? itemRepository.findByGenre(genre, pageable)
						:itemRepository.findByGenreAndManufacturer(genre, manufacturer, pageable);
		return PageResponse.<ItemDTO>builder().content(result.getContent().stream().map(ItemMapper::toDTO).collect(Collectors.toList()))
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
	}
}
