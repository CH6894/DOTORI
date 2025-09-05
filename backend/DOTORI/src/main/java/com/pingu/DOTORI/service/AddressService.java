package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.AddressResponseDTO;
import com.pingu.DOTORI.entity.Address;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.repository.AddressRepository;
import com.pingu.DOTORI.repository.UsersRepository;
import com.pingu.DOTORI.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final UsersRepository usersRepository;
    private final JwtProvider jwtProvider;

    /** ✅ 현재 로그인 유저 조회 (public으로 변경) */
    public Users getCurrentUser(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("JWT 토큰이 없습니다.");
        }
        String token = authHeader.substring(7);
        String email = jwtProvider.getSubject(token);
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + email));
    }

    /** ✅ 내 주소 조회 */
    @Transactional(readOnly = true)
    public Address getMyAddress(HttpServletRequest req) {
        Users user = getCurrentUser(req);
        return addressRepository.findFirstByUser(user).orElse(null);
    }

    /** ✅ 주소 저장 (없으면 생성, 있으면 수정) */
    @Transactional
    public Address saveOrUpdateAddress(HttpServletRequest req, AddressResponseDTO dto) {
        Users user = getCurrentUser(req);

        Address address = addressRepository.findFirstByUser(user)
                .orElse(Address.builder().user(user).build());

        String fullAddress = String.format("[%s] %s %s",
                dto.getPostcode() != null ? dto.getPostcode() : "",
                dto.getAddr1() != null ? dto.getAddr1() : "",
                dto.getAddr2() != null ? dto.getAddr2() : "");

        address.setMainAddress(fullAddress);

        return addressRepository.save(address);
    }
}

