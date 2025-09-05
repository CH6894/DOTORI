package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.OrdersRequestDTO;
import com.pingu.DOTORI.dto.OrdersResponseDTO;
import com.pingu.DOTORI.entity.*;
import com.pingu.DOTORI.repository.*;
import com.pingu.DOTORI.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final CartRepository cartRepository;
    private final UsersRepository usersRepository;
    private final ItemDetailsRepository itemDetailsRepository;
    private final JwtProvider jwtProvider;

    // 현재 로그인한 유저
    private Users getCurrentUser(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("JWT 토큰이 없습니다.");
        }
        String token = authHeader.substring(7);
        String email = jwtProvider.getSubject(token);
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + email));
    }

    // 단일 상품 주문 생성 (바로구매)
    @Transactional
    public OrdersResponseDTO createSingleOrder(OrdersRequestDTO request, HttpServletRequest req) {
        Users user = getCurrentUser(req);
        LocalDateTime now = LocalDateTime.now();

        if (request.getItemDetailsId() == null) {
            throw new IllegalArgumentException("상품 상세 ID가 필요합니다.");
        }

        ItemDetails itemDetails = itemDetailsRepository.findById(request.getItemDetailsId())
                .orElseThrow(() -> new RuntimeException("상품 상세 정보를 찾을 수 없습니다."));

        Orders order = Orders.builder()
                .user(user)
                .itemDetails(itemDetails)
                .payMethod(request.getPayMethod())
                .payMessage(request.getPayMessage())
                .depositerName(request.getDepositerName())
                .payTime(now)
                .build();

        Orders saved = ordersRepository.save(order);

        return OrdersResponseDTO.builder()
                .orderId(saved.getId())
                .itemName(saved.getItemDetails().getItem().getName())
                .title(saved.getItemDetails().getItem().getTitle())
                .mainImageUrl(saved.getItemDetails().getItem().getImgUrl())
                .quantity(1)
                .price(saved.getItemDetails().getCost().longValue())
                .payTime(saved.getPayTime())
                .payMethod(saved.getPayMethod())
                .build();
    }

    // 장바구니 기반 주문 생성
    @Transactional
    public List<OrdersResponseDTO> createOrderFromCart(OrdersRequestDTO request, HttpServletRequest req) {
        Users user = getCurrentUser(req);
        LocalDateTime now = LocalDateTime.now();

        System.out.println(user.getId());
        System.out.println(request.getCartIds().get(0));

        List<OrdersResponseDTO> results = request.getCartIds().stream().map(cartId -> {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart not found: " + cartId));

            System.out.println(cart.getId());

            Orders order = Orders.builder()
                    .user(user)
                    .itemDetails(cart.getItemDetails())
                    .payMethod(request.getPayMethod())
                    .payMessage(request.getPayMessage())
                    .depositerName(request.getDepositerName())
                    .payTime(now)
                    .build();

            System.out.println(order.getUser().getId() + "------------------------------");
            System.out.println();

            Orders saved = ordersRepository.save(order);

            return OrdersResponseDTO.builder()
                    .orderId(saved.getId())
                    .itemName(saved.getItemDetails().getItem().getName())
                    .title(saved.getItemDetails().getItem().getTitle())
                    .mainImageUrl(saved.getItemDetails().getItem().getImgUrl())
                    .quantity(cart.getQuantity())
                    .price(saved.getItemDetails().getCost().longValue())
                    .payTime(saved.getPayTime())
                    .payMethod(saved.getPayMethod())
                    .build();
        }).collect(Collectors.toList());

        // 선택한 장바구니 항목 삭제
        cartRepository.deleteAllByUser(user);

        return results;
    }

    // 내 주문 내역 조회
    @Transactional(readOnly = true)
    public List<OrdersResponseDTO> getMyOrders(HttpServletRequest req) {
        Users user = getCurrentUser(req);

        List<Orders> orders = ordersRepository.findByUser(user);

        return orders.stream().map(o -> OrdersResponseDTO.builder()
                .orderId(o.getId())
                .itemName(o.getItemDetails().getItem().getName())
                .title(o.getItemDetails().getItem().getTitle())
                .mainImageUrl(o.getItemDetails().getItem().getImgUrl())
                .quantity(1)
                .price(o.getItemDetails().getCost().longValue())
                .payTime(o.getPayTime())
                .payMethod(o.getPayMethod())
                .build()).collect(Collectors.toList());
    }

    // 주문 단건 조회
    @Transactional(readOnly = true)
    public OrdersResponseDTO getOrderById(Long orderId, HttpServletRequest req) {
        Users user = getCurrentUser(req);

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인 주문만 조회할 수 있습니다.");
        }

        return OrdersResponseDTO.builder()
                .orderId(order.getId())
                .itemName(order.getItemDetails().getItem().getName())
                .title(order.getItemDetails().getItem().getTitle())
                .mainImageUrl(order.getItemDetails().getItem().getImgUrl())
                .quantity(1)
                .price(order.getItemDetails().getCost().longValue())
                .payTime(order.getPayTime())
                .payMethod(order.getPayMethod())
                .build();
    }
}
