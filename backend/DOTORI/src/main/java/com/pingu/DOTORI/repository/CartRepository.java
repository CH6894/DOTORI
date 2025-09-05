package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.Cart;
import com.pingu.DOTORI.entity.ItemDetails;
import com.pingu.DOTORI.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserAndItemDetails(Users user, ItemDetails itemDetails);

    List<Cart> findByUser(Users user);

    @Query("select c from Cart c join fetch c.itemDetails d join fetch d.item where c.user = :user")
    List<Cart> findByUserWithDetails(Users user);

    // ✅ 유저의 장바구니 전체 삭제
    void deleteAllByUser(Users user);
}
