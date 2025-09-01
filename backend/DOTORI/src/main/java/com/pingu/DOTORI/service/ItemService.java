package com.pingu.DOTORI.service;


import lombok.RequiredArgsConstructor;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pingu.DOTORI.dto.ItemDTO;
import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemService {
	private final ItemRepository itemRepository;
	
	public Page<ItemDTO> findByGenre(String genre, Pageable pageable) {
		return itemRepository.findByGenreIgnoreCase(genre, pageable).map(this::toDto);
	}
	
	private ItemDTO toDto(Item i) {
		return ItemDTO.builder()
				.itemCode(i.getItemCode())
				.name(i.getName())
				.title(i.getTitle())
				.manufacturer(i.getManufacturer())
				.material(i.getMaterial())
				.releaseMonth(i.getReleaseMonth())
				.size(i.getSize())
				.information(i.getInformation())
				.imgUrl(i.getImgUrl())
				.storageFees(i.getStorageFees())
				.genre(i.getGenre())
				.cost(i.getCost())
				.build();
	}
	
	
}
