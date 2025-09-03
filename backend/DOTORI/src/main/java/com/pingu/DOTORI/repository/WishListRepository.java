package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishListRepository extends JpaRepository<WishList, Long> {
    boolean existsByUser_IdAndItemId(Long userId, Long itemId);
    void deleteByUser_IdAndItemId(Long userId, Long itemId);
    List<WishList> findAllByUser_IdOrderByWishListIdAsc(Long userId);
}
