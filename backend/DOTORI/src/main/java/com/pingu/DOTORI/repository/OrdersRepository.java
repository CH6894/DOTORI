package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.Orders;
import com.pingu.DOTORI.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    
    // 사용자별 주문 내역 조회 (최신순)
    List<Orders> findByUserOrderByPayTimeDesc(Users user);
    
    // 기존 메서드 유지
    List<Orders> findByUser(Users user);
    
    // 특정 시간 범위의 주문들 조회 (주문완료 페이지용)
    @Query("SELECT o FROM Orders o WHERE o.user = :user AND o.payTime BETWEEN :startTime AND :endTime ORDER BY o.payTime DESC")
    List<Orders> findOrdersByUserAndTimeRange(@Param("user") Users user, 
                                            @Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);
    
    // 정확한 시간으로 조회 (백업용)
    @Query("SELECT o FROM Orders o WHERE o.user = :user AND o.payTime = :payTime")
    List<Orders> findOrdersByUserAndExactTime(@Param("user") Users user, 
                                            @Param("payTime") LocalDateTime payTime);
}