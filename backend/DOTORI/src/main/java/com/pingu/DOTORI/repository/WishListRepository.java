package com.pingu.DOTORI.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pingu.DOTORI.entity.WishList;

@Repository
public interface WishListRepository extends JpaRepository<WishList, Long> {

    // WishList.user.id 와 WishList.itemDetails.itemId 를 조건으로 찾기
    Optional<WishList> findByUser_IdAndItemDetails_ItemId(Long userId, Long itemId);

    // WishList.user.id 와 WishList.isLiked 조건
    List<WishList> findByUser_IdAndIsLiked(Long userId, Boolean isLiked);
}