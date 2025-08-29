package com.pingu.DOTORI.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pingu.DOTORI.entity.ItemDetails;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.entity.WishList;
import com.pingu.DOTORI.repository.ItemDetailsRepository;
import com.pingu.DOTORI.repository.UsersRepository;
import com.pingu.DOTORI.repository.WishListRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WishListService {

   private final WishListRepository wishListRepository;
   private final UsersRepository usersRepository;
   private final ItemDetailsRepository itemDetailsRepository;

   // 위시리스트 토글
   public boolean toggleWishList(String email, Long itemDetailsId) {
       Users user = usersRepository.findByEmail(email)
               .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

       // ✅ 메서드명 변경: findByUser_IdAndItemDetails_ItemId
       Optional<WishList> existingWishList =
               wishListRepository.findByUser_IdAndItemDetails_ItemId(user.getId(), itemDetailsId);

       if (existingWishList.isPresent()) {
           WishList wishList = existingWishList.get();
           wishList.setIsLiked(false);
           wishListRepository.save(wishList);
           return false;
       } else {
           ItemDetails itemDetails = itemDetailsRepository.findById(itemDetailsId)
                   .orElseThrow(() -> new RuntimeException("아이템을 찾을 수 없습니다."));

           WishList newWishList = new WishList();
           newWishList.setUser(user);
           newWishList.setItemDetails(itemDetails);
           newWishList.setIsLiked(true);
           wishListRepository.save(newWishList);
           return true;
       }
   }

   // 유저의 위시리스트 목록 조회
   public List<WishList> getUserWishList(String email) {
       Users user = usersRepository.findByEmail(email)
               .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

       // ✅ 메서드명 변경: findByUser_IdAndIsLiked
       return wishListRepository.findByUser_IdAndIsLiked(user.getId(), true);
   }
}
