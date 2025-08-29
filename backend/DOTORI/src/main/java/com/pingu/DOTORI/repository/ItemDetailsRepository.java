package com.pingu.DOTORI.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pingu.DOTORI.entity.ItemDetails;

@Repository
public interface ItemDetailsRepository extends JpaRepository<ItemDetails, Long> {
	List<ItemDetails> findByItemIdAndUnpacked(Long itemId, Byte unpacked);
}
