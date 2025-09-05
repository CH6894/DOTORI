package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.Orders;
import com.pingu.DOTORI.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByUser(Users user);
}
