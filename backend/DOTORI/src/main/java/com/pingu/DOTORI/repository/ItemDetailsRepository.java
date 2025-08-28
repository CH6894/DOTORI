package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.ItemDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemDetailsRepository extends JpaRepository<ItemDetails, Long> {

  
  Optional<ItemDetails> findByItemId(Long itemId);

 
  Page<ItemDetails> findByUser_Id(Long userId, Pageable pageable);

  Page<ItemDetails> findByStatus(Boolean status, Pageable pageable);
}
