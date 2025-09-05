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

    // 현재 로그인한 유저 조회
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

        // 이미 판매된 상품인지 확인
        if (!itemDetails.getStatus()) {
            throw new RuntimeException("이미 판매된 상품입니다.");
        }

        // 주문 생성
        Orders order = Orders.builder()
                .user(user)
                .itemDetails(itemDetails)
                .payMethod(request.getPayMethod())
                .payMessage(request.getPayMessage())
                .depositerName(request.getDepositerName())
                .payTime(now)
                .build();

        Orders saved = ordersRepository.save(order);

        // 상품 상태를 판매완료(false)로 변경하고 배송일 설정
        itemDetails.setStatus(false);
        itemDetails.setDeliveryDate(now);
        itemDetailsRepository.save(itemDetails);

        return OrdersResponseDTO.builder()
                .orderId(saved.getId())
                .itemName(saved.getItemDetails().getItem().getName())
                .title(saved.getItemDetails().getItem().getTitle())
                .mainImageUrl(saved.getItemDetails().getItem().getImgUrl())
                .quantity(1) // 중고상품은 항상 1개
                .price(saved.getItemDetails().getCost().longValue())
                .payTime(saved.getPayTime())
                .payMethod(saved.getPayMethod())
                .depositerName(saved.getDepositerName())
                .payMessage(saved.getPayMessage())
                .build();
    }

    // 장바구니 기반 주문 생성
    @Transactional
    public List<OrdersResponseDTO> createOrderFromCart(OrdersRequestDTO request, HttpServletRequest req) {
        Users user = getCurrentUser(req);
        LocalDateTime now = LocalDateTime.now();

        System.out.println("User ID: " + user.getId());
        System.out.println("Cart IDs: " + request.getCartIds());
        
        // 선택된 장바구니 항목들 조회
        List<Cart> selectedCarts = cartRepository.findAllById(request.getCartIds());
        
        // 유저 소유 확인
        boolean allCartsOwnedByUser = selectedCarts.stream()
                .allMatch(cart -> cart.getUser().getId().equals(user.getId()));
        
        if (!allCartsOwnedByUser) {
            throw new RuntimeException("본인의 장바구니 항목만 주문할 수 있습니다.");
        }
        
        // 모든 상품이 판매 가능한 상태인지 확인
        List<ItemDetails> soldOutItems = selectedCarts.stream()
                .map(Cart::getItemDetails)
                .filter(itemDetails -> !itemDetails.getStatus())
                .collect(Collectors.toList());
        
        if (!soldOutItems.isEmpty()) {
            String soldOutItemNames = soldOutItems.stream()
                    .map(item -> item.getItem().getName())
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("다음 상품들이 이미 판매되었습니다: " + soldOutItemNames);
        }
        
        List<OrdersResponseDTO> results = selectedCarts.stream().map(cart -> {
            System.out.println("Processing cart ID: " + cart.getId());

            // 주문 생성
            Orders order = Orders.builder()
                    .user(user)
                    .itemDetails(cart.getItemDetails())
                    .payMethod(request.getPayMethod())
                    .payMessage(request.getPayMessage())
                    .depositerName(request.getDepositerName())
                    .payTime(now)
                    .build();

            Orders saved = ordersRepository.save(order);

            // 상품 상태를 판매완료(false)로 변경하고 배송일 설정
            ItemDetails itemDetails = cart.getItemDetails();
            itemDetails.setStatus(false);
            itemDetails.setDeliveryDate(now);
            itemDetailsRepository.save(itemDetails);

            return OrdersResponseDTO.builder()
                    .orderId(saved.getId())
                    .itemName(saved.getItemDetails().getItem().getName())
                    .title(saved.getItemDetails().getItem().getTitle())
                    .mainImageUrl(saved.getItemDetails().getItem().getImgUrl())
                    .quantity(1) // 중고상품은 항상 1개
                    .price(saved.getItemDetails().getCost().longValue())
                    .payTime(saved.getPayTime())
                    .payMethod(saved.getPayMethod())
                    .depositerName(saved.getDepositerName())
                    .payMessage(saved.getPayMessage())
                    .build();
        }).collect(Collectors.toList());

        // 선택한 장바구니 항목만 삭제
        cartRepository.deleteAllById(request.getCartIds());

        return results;
    }

    // 내 주문 내역 조회
    @Transactional(readOnly = true)
    public List<OrdersResponseDTO> getMyOrders(HttpServletRequest req) {
        Users user = getCurrentUser(req);

        // 최신 주문부터 조회
        List<Orders> orders = ordersRepository.findByUserOrderByPayTimeDesc(user);

        return orders.stream().map(order -> OrdersResponseDTO.builder()
                .orderId(order.getId())
                .itemName(order.getItemDetails().getItem().getName())
                .title(order.getItemDetails().getItem().getTitle())
                .mainImageUrl(order.getItemDetails().getItem().getImgUrl())
                .quantity(1) // 중고상품은 항상 1개
                .price(order.getItemDetails().getCost().longValue())
                .payTime(order.getPayTime())
                .payMethod(order.getPayMethod())
                .depositerName(order.getDepositerName())
                .payMessage(order.getPayMessage())
                .build()
        ).collect(Collectors.toList());
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
                .quantity(1) // 중고상품은 항상 1개
                .price(order.getItemDetails().getCost().longValue())
                .payTime(order.getPayTime())
                .payMethod(order.getPayMethod())
                .depositerName(order.getDepositerName())
                .payMessage(order.getPayMessage())
                .build();
    }

    // payTime 기준으로 주문 그룹 조회 (주문 완료 페이지용)
    // 같은 시간에 여러 상품을 주문한 경우 그룹으로 조회
    @Transactional(readOnly = true)
    public List<OrdersResponseDTO> getOrdersByPayTime(String payTimePrefix, HttpServletRequest req) {
        Users user = getCurrentUser(req);
        
        System.out.println("=== getOrdersByPayTime 디버깅 ===");
        System.out.println("받은 payTimePrefix: " + payTimePrefix);
        
        try {
            // payTimePrefix를 LocalDateTime으로 파싱
            LocalDateTime targetTime;
            
            // 다양한 형식 지원
            if (payTimePrefix.length() == 19) {
                // "2025-09-05T02:37:12" 형식
                targetTime = LocalDateTime.parse(payTimePrefix);
            } else if (payTimePrefix.length() == 16) {
                // "2025-09-05T02:37" 형식
                targetTime = LocalDateTime.parse(payTimePrefix + ":00");
            } else {
                throw new IllegalArgumentException("지원하지 않는 날짜 형식: " + payTimePrefix);
            }
            
            System.out.println("파싱된 targetTime: " + targetTime);
            
            // ± 2분 범위로 검색하여 같은 시간대 주문들을 모두 찾기
            LocalDateTime startTime = targetTime.minusMinutes(2);
            LocalDateTime endTime = targetTime.plusMinutes(2);
            
            System.out.println("검색 범위: " + startTime + " ~ " + endTime);
            
            List<Orders> orders = ordersRepository.findOrdersByUserAndTimeRange(user, startTime, endTime);
            System.out.println("범위 검색 결과 수: " + orders.size());
            
            // 결과가 없으면 정확한 시간으로 다시 시도
            if (orders.isEmpty()) {
                System.out.println("범위 검색 실패, 정확한 시간으로 재시도");
                orders = ordersRepository.findOrdersByUserAndExactTime(user, targetTime);
                System.out.println("정확한 시간 검색 결과 수: " + orders.size());
            }
            
            // 여전히 없으면 전체 주문에서 검색
            if (orders.isEmpty()) {
                System.out.println("정확한 시간 검색도 실패, 전체 주문에서 검색");
                List<Orders> allOrders = ordersRepository.findByUserOrderByPayTimeDesc(user);
                System.out.println("사용자 전체 주문 수: " + allOrders.size());
                
                for (Orders order : allOrders) {
                    System.out.println("주문 ID: " + order.getId() + ", payTime: " + order.getPayTime());
                }
            }
            
            List<OrdersResponseDTO> result = orders.stream().map(order -> {
                System.out.println("매핑 중인 주문: ID=" + order.getId() + ", payTime=" + order.getPayTime());
                return OrdersResponseDTO.builder()
                        .orderId(order.getId())
                        .itemName(order.getItemDetails().getItem().getName())
                        .title(order.getItemDetails().getItem().getTitle())
                        .mainImageUrl(order.getItemDetails().getItem().getImgUrl())
                        .quantity(1) // 중고상품은 항상 1개
                        .price(order.getItemDetails().getCost().longValue())
                        .payTime(order.getPayTime())
                        .payMethod(order.getPayMethod())
                        .depositerName(order.getDepositerName())
                        .payMessage(order.getPayMessage())
                        .build();
            }).collect(Collectors.toList());
            
            System.out.println("최종 반환 결과 수: " + result.size());
            return result;
            
        } catch (Exception e) {
            System.err.println("payTime 파싱 오류: " + e.getMessage());
            e.printStackTrace();
            
            // 파싱 실패 시 전체 주문에서 필터링하는 방식으로 폴백
            List<OrdersResponseDTO> allOrders = getMyOrders(req);
            System.out.println("폴백: 전체 주문 수 " + allOrders.size());
            
            return allOrders.stream()
                    .filter(order -> {
                        String orderTimeStr = order.getPayTime().toString();
                        boolean matches = orderTimeStr.startsWith(payTimePrefix) || 
                                         orderTimeStr.substring(0, Math.min(16, orderTimeStr.length())).equals(payTimePrefix.substring(0, Math.min(16, payTimePrefix.length())));
                        System.out.println("필터링: " + orderTimeStr + " vs " + payTimePrefix + " = " + matches);
                        return matches;
                    })
                    .collect(Collectors.toList());
        }
    }
}