package com.pingu.DOTORI.service;


import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
=======
import java.util.List;

>>>>>>> 051fab3ea31a8c59d0f6ae6bbb7a5475fd854e22
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pingu.DOTORI.dto.ItemDTO;
import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemService {
<<<<<<< HEAD
	private ItemRepository itemRepository;
	public Page<Item> findAll(Pageable pageable){ return itemRepository.findAll(pageable); }
=======
	private final ItemRepository itemRepository;
	
	public Page<ItemDTO> findAll(Pageable pageable) {
		return itemRepository.findAll(pageable).map(this::toDto);
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
>>>>>>> 051fab3ea31a8c59d0f6ae6bbb7a5475fd854e22
}
