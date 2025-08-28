package com.pingu.DOTORI.service;


import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemService {
	private final ItemRepository itemRepository;
	public List<Item> findAll() {
        // 아주 단순: 전부 조회
        return itemRepository.findAll();
    }
	
	public Item findOne(String itemCode) {
		return itemRepository.findById(itemCode).orElseThrow(() -> new IllegalArgumentException("Item not found:" + itemCode));
	}
}
