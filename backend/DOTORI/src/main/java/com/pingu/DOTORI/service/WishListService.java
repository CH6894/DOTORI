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
        Long itemId = itemDetails.getItemId();

        return wishListRepository.findByUserIdAndItemId(userId, itemId)
            .map(w -> {
                w.setIsLiked(Boolean.TRUE);
                return w;
            })
            .orElseGet(() -> {
                WishList w = WishList.builder()
                        .user(user)
                        .itemDetails(itemDetails)
                        .isLiked(Boolean.TRUE)
                        .build();
                return wishListRepository.save(w);
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
                w.setIsLiked(Boolean.FALSE);
                return true;
            })
            .orElse(false);
    }
}


