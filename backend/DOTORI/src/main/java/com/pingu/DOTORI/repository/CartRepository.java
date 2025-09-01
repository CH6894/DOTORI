package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // 회원(userId)의 장바구니 전체 조회
    List<Cart> findByUser_Id(Long userId);

    // 회원(userId)의 장바구니 전체 비우기
    void deleteByUser_Id(Long userId);

    // 특정 회원이 특정 상품(ItemDetails)을 이미 장바구니에 담았는지 확인
    Optional<Cart> findByUser_IdAndItemDetails_ItemId(Long userId, Long itemDetailsId);

    // 회원(userId)의 장바구니 목록을 최신순(cart_id DESC)으로 조회
    List<Cart> findByUser_IdOrderByIdDesc(Long userId);
}
