//package com.pingu.DOTORI.repository;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import com.pingu.DOTORI.entity.WishList;
//
//public interface WishListRepository extends JpaRepository<WishList, Long> {
//	boolean existsByUserUserIdAndItemItemId(Long userId, Long itemId);
//	void deleteByUserUserIdAndItemItemId(Long userId, Long itemId);
//	Page<WishList> findByUserUserId(Long userId, Pageable pageable);
//}
