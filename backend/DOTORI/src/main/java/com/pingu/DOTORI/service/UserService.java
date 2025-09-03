package com.pingu.DOTORI.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.repository.ItemRepository;

@Service
public class UserService {

	@Autowired
	ItemRepository itemRepository;

	public Optional<Item> find(String test) {
		return itemRepository.findById(test);
	}

}