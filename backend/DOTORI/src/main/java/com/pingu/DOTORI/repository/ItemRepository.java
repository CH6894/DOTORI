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
	Optional<Item> findByItemCode(String itemCode);

	Page<Item> findByGenreIgnoreCase(String genre, Pageable pageable);

	Page<Item> findByGenreIgnoreCaseAndTitleContainingIgnoreCase(String genre, String title, Pageable pageable);

	// 검색어로 아이템 찾기 (name, title, genre에서 검색)
	Page<Item> findByNameContainingIgnoreCaseOrTitleContainingIgnoreCaseOrGenreContainingIgnoreCase(
			String name, String title, String genre, Pageable pageable);

}
