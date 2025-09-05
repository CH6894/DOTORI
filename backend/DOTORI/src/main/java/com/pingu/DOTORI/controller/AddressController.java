package com.pingu.DOTORI.controller;

import java.util.Arrays;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pingu.DOTORI.dto.AddressResponseDTO;
import com.pingu.DOTORI.entity.Address;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.service.AddressService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /** ✅ 내 주소 조회 */
    @GetMapping
    public AddressResponseDTO getMyAddress(HttpServletRequest req) {
        Address address = addressService.getMyAddress(req);
        Users user = addressService.getCurrentUser(req);

        if (address == null) {
            // 주소는 없지만 유저 정보는 내려줌
            return AddressResponseDTO.builder()
                    .receiver(user.getUserName())
                    .phone(user.getPhone())
                    .postcode("")
                    .addr1("")
                    .addr2("")
                    .mainAddress("")
                    .build();
        }

        // 주소 + 유저 정보 같이 내려줌
        return AddressResponseDTO.builder()
                .addressId(address.getId())
                .receiver(user.getUserName())
                .phone(user.getPhone())
                .postcode(extractPostcode(address.getMainAddress()))
                .addr1(extractAddr1(address.getMainAddress()))
                .addr2(extractAddr2(address.getMainAddress()))
                .mainAddress(address.getMainAddress())
                .build();
    }

    /** ✅ 내 주소 저장 */
    @PostMapping
    public Address saveAddress(HttpServletRequest req, @RequestBody AddressResponseDTO dto) {
        return addressService.saveOrUpdateAddress(req, dto);
    }

    // --- 유틸 메서드 ---
    private String extractPostcode(String mainAddress) {
        if (mainAddress == null || !mainAddress.startsWith("[")) return "";
        int endIdx = mainAddress.indexOf("]");
        return endIdx > 0 ? mainAddress.substring(1, endIdx) : "";
    }

    private String extractAddr1(String mainAddress) {
        if (mainAddress == null) return "";
        int endIdx = mainAddress.indexOf("]");
        if (endIdx < 0) return "";
        String[] parts = mainAddress.substring(endIdx + 1).trim().split(" ");
        return parts.length > 0 ? parts[0] : "";
    }

    private String extractAddr2(String mainAddress) {
        if (mainAddress == null) return "";
        int endIdx = mainAddress.indexOf("]");
        if (endIdx < 0) return "";
        String[] parts = mainAddress.substring(endIdx + 1).trim().split(" ");
        return parts.length > 1 ? String.join(" ", Arrays.copyOfRange(parts, 1, parts.length)) : "";
    }
}
