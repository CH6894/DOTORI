package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.ItemDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemDetailsRepository extends JpaRepository<ItemDetails, Long> {

  Optional<ItemDetails> findFirstByItem_ItemCodeAndUser_Id(String itemCode, Long userId);
 
  Page<ItemDetails> findByUser_Id(Long userId, Pageable pageable);

  Page<ItemDetails> findByStatus(Boolean status, Pageable pageable);

  // 승인된 상품의 ItemDetails를 itemCode로 조회
  Optional<ItemDetails> findByItem_ItemCodeAndStatus(String itemCode, Boolean status);
  
  // 승인된 상품의 ItemDetails를 itemCode로 조회 (개봉 여부 포함)
  List<ItemDetails> findByItem_ItemCodeAndStatusOrderByUnpackedAsc(String itemCode, Boolean status);
  
  // 승인된 미개봉 상품의 ItemDetails만 조회
  List<ItemDetails> findByItem_ItemCodeAndStatusAndUnpackedFalse(String itemCode, Boolean status);
  
  // 승인된 개봉 상품의 ItemDetails만 조회
  List<ItemDetails> findByItem_ItemCodeAndStatusAndUnpackedTrue(String itemCode, Boolean status);
  
  	// 승인된 개봉 상품의 ItemDetails와 Admin 정보를 함께 조회 (Admin.admission_state = 1 확인)
	@Query("SELECT DISTINCT id FROM ItemDetails id " +
		   "LEFT JOIN FETCH id.admins a " +
		   "LEFT JOIN FETCH id.item " +
		   "WHERE id.item.itemCode = :itemCode AND id.unpacked = true " +
		   "AND a.admissionState = 1 " +
		   "ORDER BY id.itemId")
	List<ItemDetails> findApprovedOpenedItemDetailsWithAdminByItemCode(@Param("itemCode") String itemCode);
	
	// 승인된 미개봉 상품의 ItemDetails와 Admin 정보를 함께 조회 (Admin.admission_state = 1 확인)
	@Query("SELECT DISTINCT id FROM ItemDetails id " +
		   "LEFT JOIN FETCH id.admins a " +
		   "LEFT JOIN FETCH id.item " +
		   "WHERE id.item.itemCode = :itemCode AND id.unpacked = false " +
		   "AND a.admissionState = 1 " +
		   "ORDER BY id.itemId")
	List<ItemDetails> findApprovedUnpackedItemDetailsWithAdminByItemCode(@Param("itemCode") String itemCode);
}
