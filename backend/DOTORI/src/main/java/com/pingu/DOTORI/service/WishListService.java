package com.pingu.DOTORI.service;

import java.util.List;
// import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pingu.DOTORI.entity.ItemDetails;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.entity.WishList;
import com.pingu.DOTORI.repository.UsersRepository;
import com.pingu.DOTORI.repository.WishListRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;
    private final UsersRepository usersRepository; // reserved for future use

    @Transactional
    public WishList toggleLike(Users user, ItemDetails itemDetails) {
        Long userId = user.getId();
        Long itemDetailsId = itemDetails.getItemId();

        return wishListRepository.findByUserIdAndItemDetailsId(userId, itemDetailsId)
            .map(w -> {
                // 이미 좋아요가 있으면 삭제 (토글)
                wishListRepository.delete(w);
                return null;
            })
            .orElseGet(() -> {
                // 좋아요가 없으면 새로 생성
                WishList wishList = WishList.builder()
                        .user(user)
                        .itemDetails(itemDetails)
                        .isLiked(Boolean.TRUE)
                        .build();
                return wishListRepository.save(wishList);
            });
    }

    @Transactional(readOnly = true)
    public List<WishList> getLikedByUserId(Long userId) {
        return wishListRepository.findAllLikedWithJoinsByUserId(userId);
    }

    @Transactional
    public boolean unlikeByWishlistId(Long wishlistId, Long userId) {
        return wishListRepository.findById(wishlistId)
            .filter(w -> w.getUser() != null && w.getUser().getId().equals(userId))
            .map(w -> {
                wishListRepository.delete(w);  // 실제 삭제
                return true;
            })
            .orElse(false);
    }
}


