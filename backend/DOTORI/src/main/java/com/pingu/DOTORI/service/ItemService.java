package com.pingu.DOTORI.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemService {
	private final ItemRepository itemRepository;
	public Page<Item> findAll(Pageable pageable){ return itemRepository.findAll(pageable); }
}
