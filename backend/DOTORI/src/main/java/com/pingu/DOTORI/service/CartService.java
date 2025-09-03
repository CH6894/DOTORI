package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.CartResponse;
import com.pingu.DOTORI.entity.Cart;
import com.pingu.DOTORI.entity.ItemDetails;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.repository.CartRepository;
import com.pingu.DOTORI.repository.ItemDetailsRepository;
import com.pingu.DOTORI.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// 장바구니 서비스 (세션 로그인 기반)

@Service
@RequiredArgsConstructor
public class CartService {

        private final CartRepository cartRepository;
        private final UsersRepository usersRepository;
        private final ItemDetailsRepository itemDetailsRepository;

        // 장바구니 담기 이미 있으면 수량 +1 없으면 새로 생성
        @Transactional
        public Cart addCart(Long userId, Long itemDetailsId) {
                Users user = usersRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
                ItemDetails itemDetails = itemDetailsRepository.findById(itemDetailsId)
                                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

                Optional<Cart> existingCart = cartRepository.findByUser_IdAndItemDetails_ItemId(userId, itemDetailsId);

                if (existingCart.isPresent()) {
                        Cart found = existingCart.get();
                        found.setQuantity(found.getQuantity() + 1);
                        return cartRepository.save(found);
                } else {
                        Cart cart = Cart.builder()
                                        .user(user)
                                        .itemDetails(itemDetails)
                                        .quantity(1)
                                        .build();
                        return cartRepository.save(cart);
                }
        }

        // 장바구니 조회 (최신순)

        public List<CartResponse> getCartByUser(Long userId) {
                return cartRepository.findByUser_IdOrderByIdDesc(userId)
                                .stream()
                                .map(cart -> CartResponse.builder()
                                                .cartId(cart.getId())
                                                .itemDetailsId(cart.getItemDetails().getItemId())
                                                .itemName(cart.getItemDetails().getItem() != null
                                                                ? cart.getItemDetails().getItem().getName()
                                                                : null)
                                                .quantity(cart.getQuantity())
                                                .price(cart.getItemDetails().getCost() != null
                                                                ? cart.getItemDetails().getCost().intValue()
                                                                : 0)
                                                .thumbnailUrl(
                                                                cart.getItemDetails().getImages().isEmpty()
                                                                                ? null
                                                                                : cart.getItemDetails().getImages()
                                                                                                .get(0).getImgUrl())
                                                .build())
                                .collect(Collectors.toList());
        }

        // 장바구니 수량 변경

        @Transactional
        public Cart updateQuantity(Long cartId, int quantity, Long userId) {
                Cart cart = cartRepository.findById(cartId)
                                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목 없음"));

                if (!cart.getUser().getId().equals(userId)) {
                        throw new IllegalStateException("본인 장바구니만 수정 가능");
                }

                cart.setQuantity(Math.max(1, quantity));
                return cartRepository.save(cart);
        }

        // 장바구니 전체 비우기

        @Transactional
        public void clearCart(Long userId) {
                cartRepository.deleteByUser_Id(userId);
        }

        // 장바구니 단일 항목 삭제 (본인 확인 포함)

        @Transactional
        public void removeItem(Long cartId, Long userId) {
                Cart cart = cartRepository.findById(cartId)
                                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목 없음"));

                if (!cart.getUser().getId().equals(userId)) {
                        throw new IllegalStateException("본인 장바구니만 삭제 가능");
                }

                cartRepository.delete(cart);
        }
}
