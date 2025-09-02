package com.pingu.DOTORI.repository;

import java.util.List;
import java.util.Optional;

// import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pingu.DOTORI.entity.WishList;

@Repository
public interface WishListRepository extends JpaRepository<WishList, Long> {

    @Query("SELECT w FROM WishList w WHERE w.user.id = :userId AND w.itemDetails.itemId = :itemDetailsId")
    Optional<WishList> findByUserIdAndItemDetailsId(@Param("userId") Long userId, @Param("itemDetailsId") Long itemDetailsId);

    @Query("SELECT w FROM WishList w " +
           "JOIN FETCH w.itemDetails idet " +
           "JOIN FETCH idet.item i " +
           "WHERE w.user.id = :userId AND w.isLiked = true")
    List<WishList> findAllLikedWithJoinsByUserId(@Param("userId") Long userId);
}
