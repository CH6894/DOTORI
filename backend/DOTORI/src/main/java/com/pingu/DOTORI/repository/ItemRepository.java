package com.pingu.DOTORI.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pingu.DOTORI.entity.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
	
	// 장르별 아이템 페이징 조회
	Page<Item> findByGenre(String genre, Pageable pageable);
	
	// 제조사 별 아이템 페이징 조회
	Page<Item> findByGenreAndManufacturer(String Genre, String manufacturer, Pageable pageable);
}
