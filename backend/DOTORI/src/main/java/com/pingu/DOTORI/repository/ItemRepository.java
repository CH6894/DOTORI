package com.pingu.DOTORI.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pingu.DOTORI.entity.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
	// 
	Page<Item> findByGenreIgnoreCase(String genre, Pageable pageable);
	
	Page<Item> findByGenreIgnoreCaseAndTitleContainingIgnoreCase(String genre, String title, Pageable pageable);
}
