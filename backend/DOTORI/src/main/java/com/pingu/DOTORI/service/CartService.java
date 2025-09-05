package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.AddCartRequest;
import com.pingu.DOTORI.dto.CartResponseDTO;
import com.pingu.DOTORI.entity.Cart;
import com.pingu.DOTORI.entity.ItemDetails;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.repository.CartRepository;
import com.pingu.DOTORI.repository.ItemDetailsRepository;
import com.pingu.DOTORI.repository.UsersRepository;
import com.pingu.DOTORI.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UsersRepository usersRepository;
    private final ItemDetailsRepository itemDetailsRepository;
    private final JwtProvider jwtProvider;

    /** ✅ application.yml 에서 base-url 가져오기 */
    @Value("${dotori.base-url}")
    private String baseUrl;

    /** ✅ JWT에서 유저 조회 */
    private Users getUserFromRequest(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("JWT 토큰이 없습니다.");
        }
        String token = authHeader.substring(7);
        String email = jwtProvider.getSubject(token);

        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다: " + email));
    }

    /** ✅ 장바구니 조회 */
    public List<CartResponseDTO> getMyCart(HttpServletRequest req) {
        Users user = getUserFromRequest(req);

        return cartRepository.findByUserWithDetails(user).stream()
                .map(this::toDto)
                .toList();
    }

    /** ✅ 장바구니 담기 (중복 상품 수량 증가 처리) */
    @Transactional
    public CartResponseDTO addToCart(HttpServletRequest req, AddCartRequest request) {
        Users user = getUserFromRequest(req);

        ItemDetails itemDetails = itemDetailsRepository.findById(request.getItemDetailsId())
                .orElseThrow(() -> new IllegalArgumentException("상품 상세 정보를 찾을 수 없습니다."));

        // 이미 장바구니에 있으면 수량만 증가
        Optional<Cart> existingCart = cartRepository.findByUserAndItemDetails(user, itemDetails);

        Cart cart;
        if (existingCart.isPresent()) {
            cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + request.getQuantity());
        } else {
            cart = Cart.builder()
                    .user(user)
                    .itemDetails(itemDetails)
                    .quantity(request.getQuantity())
                    .build();
        }

        Cart savedCart = cartRepository.save(cart);
        return toDto(savedCart);
    }

    /** ✅ 수량 변경 */
    @Transactional
    public CartResponseDTO updateQuantity(HttpServletRequest req, Long cartId, int quantity) {
        Users user = getUserFromRequest(req);
        Cart cart = cartRepository.findById(cartId)
                .filter(c -> c.getUser().equals(user))
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 항목이 없습니다."));

        cart.setQuantity(quantity);
        cartRepository.save(cart);
        return toDto(cart);
    }

    /** ✅ 개별 삭제 */
    @Transactional
    public void removeCart(HttpServletRequest req, Long cartId) {
        Users user = getUserFromRequest(req);
        Cart cart = cartRepository.findById(cartId)
                .filter(c -> c.getUser().equals(user))
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 항목이 없습니다."));
        cartRepository.delete(cart);
    }

    /** ✅ 전체 삭제 */
    @Transactional
    public void clearCart(HttpServletRequest req) {
        Users user = getUserFromRequest(req);
        cartRepository.deleteAll(cartRepository.findByUser(user));
    }

    /** ✅ Entity → DTO 변환 (이미지 URL 절대 경로로) */
    private CartResponseDTO toDto(Cart c) {
        return CartResponseDTO.builder()
                .cartId(c.getId())
                .itemName(c.getItemDetails().getItem().getName())
                .title(c.getItemDetails().getItem().getTitle())
                .mainImageUrl(baseUrl + c.getItemDetails().getItem().getImgUrl()) // ✅ 절대 경로
                .quantity(c.getQuantity())
                .price(c.getItemDetails().getCost().longValue())
                .build();
    }
}
