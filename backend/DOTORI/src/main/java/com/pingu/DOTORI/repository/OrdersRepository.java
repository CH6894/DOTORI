package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    // 특정 회원(userId)의 주문 내역 최신순 조회
    List<Orders> findByUser_IdOrderByIdDesc(Long userId);
}
