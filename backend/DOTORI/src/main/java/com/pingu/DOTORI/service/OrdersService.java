package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.OrderRequest;
import com.pingu.DOTORI.dto.OrderResponse;
import com.pingu.DOTORI.entity.Address;
import com.pingu.DOTORI.entity.Cart;
import com.pingu.DOTORI.entity.Orders;
import com.pingu.DOTORI.repository.AddressRepository;
import com.pingu.DOTORI.repository.CartRepository;
import com.pingu.DOTORI.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final CartRepository cartRepository;
    private final AddressService addressService;

    // 장바구니 → 주문 생성

    @Transactional
    public List<Orders> createOrdersFromCart(Long userId, OrderRequest request) {
        // 1. 선택된 장바구니 항목 조회
        List<Cart> carts = cartRepository.findAllById(request.getCartIds());
        if (carts.isEmpty()) {
            throw new IllegalArgumentException("선택한 장바구니 항목이 없습니다.");
        }

        // 2. 기본 배송지 조회 (AddressService 활용)
        var addressDto = addressService.findFirstByUserId(userId);

        List<Orders> orders = new ArrayList<>();

        // 3. 장바구니 → 주문 변환
        for (Cart cart : carts) {
            if (!cart.getUser().getId().equals(userId)) {
                throw new IllegalStateException("본인 장바구니만 주문할 수 있습니다.");
            }

            Orders order = Orders.builder()
                    .user(cart.getUser())
                    .itemDetails(cart.getItemDetails())
                    .payMethod("무통장") // 무조건 무통장 결제
                    .depositerName(request.getDepositerName())
                    .payMessage("배송지: " + addressDto.getMainAddress())
                    .payTime(LocalDateTime.now())
                    .build();

            orders.add(order);
        }

        // 4. 주문 저장
        List<Orders> savedOrders = ordersRepository.saveAll(orders);

        // 5. 주문 완료된 장바구니 항목 삭제
        cartRepository.deleteAll(carts);

        return savedOrders;
    }

    // 나의 주문 내역 조회 (최신순)
    public List<Orders> getOrdersByUser(Long userId) {
        return ordersRepository.findByUser_IdOrderByIdDesc(userId);
    }
}
