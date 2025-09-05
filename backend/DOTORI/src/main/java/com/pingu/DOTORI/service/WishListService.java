package com.pingu.DOTORI.service;

import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.entity.WishList;
import com.pingu.DOTORI.entity.ItemDetails;
import com.pingu.DOTORI.repository.WishListRepository;
import com.pingu.DOTORI.repository.ItemDetailsRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class WishListService {
    private final WishListRepository repo;
    private final ItemDetailsRepository itemDetailsRepository;
    private final EntityManager em;

    public void add(Long userId, Long itemId) {
        if (!repo.existsByUser_IdAndItemId(userId, itemId)) {
            Users userRef = em.getReference(Users.class, userId);
            WishList w = new WishList();
            w.setUser(userRef);
            w.setItemId(itemId);
            repo.save(w);
        }
    }

    public void remove(Long userId, Long itemId) {
        repo.deleteByUser_IdAndItemId(userId, itemId);
    }

    @Transactional(readOnly = true)
    public List<WishList> list(Long userId) {
        return repo.findAllByUser_IdOrderByWishListIdAsc(userId);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listWithItemDetails(Long userId) {
        List<WishList> wishLists = repo.findAllByUser_IdOrderByWishListIdAsc(userId);

        return wishLists.stream().map(wishList -> {
            Map<String, Object> result = new HashMap<>();
            result.put("wishListId", wishList.getWishListId());
            result.put("itemId", wishList.getItemId());

            // ItemDetails 정보 조회
            ItemDetails itemDetails = itemDetailsRepository.findById(wishList.getItemId()).orElse(null);
            if (itemDetails != null) {
                result.put("title", itemDetails.getItem().getTitle());
                result.put("name", itemDetails.getItem().getName()); // 상품 이름 추가
                result.put("price", itemDetails.getCost());
                result.put("unpacked", itemDetails.getUnpacked());
                result.put("status", itemDetails.getStatus());
                result.put("itemCode", itemDetails.getItem().getItemCode()); // itemCode 추가

                // 이미지 정보 (첫 번째 이미지)
                if (itemDetails.getImages() != null && !itemDetails.getImages().isEmpty()) {
                    result.put("image", itemDetails.getImages().get(0).getImgUrl());
                } else {
                    result.put("image", "/img/placeholder.jpg");
                }
            } else {
                result.put("title", "상품 정보 없음");
                result.put("name", "상품 정보 없음");
                result.put("price", 0);
                result.put("unpacked", false);
                result.put("status", false);
                result.put("itemCode", "");
                result.put("image", "/img/placeholder.jpg");
            }

            return result;
        }).toList();
    }
}
