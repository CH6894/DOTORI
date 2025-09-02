package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.AddressRequest;
import com.pingu.DOTORI.dto.AddressResponse;
import com.pingu.DOTORI.entity.Address;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.repository.AddressRepository;
import com.pingu.DOTORI.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UsersRepository usersRepository;

    public List<AddressResponse> findByUserId(Long userId) {
        return addressRepository.findByUser_Id(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public AddressResponse addAddress(Long userId, AddressRequest request) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Address address = Address.builder()
                .mainAddress(request.getAddr1() + " " + request.getAddr2())
                .user(user)
                .build();

        return toResponse(addressRepository.save(address));
    }

    public AddressResponse updateAddress(Long userId, Long id, AddressRequest request) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        address.setMainAddress(request.getAddr1() + " " + request.getAddr2());

        return toResponse(addressRepository.save(address));
    }

    public void deleteAddress(Long userId, Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        addressRepository.delete(address);
    }
    
 
    public AddressResponse findFirstByUserId(Long userId) {
        return addressRepository.findFirstByUser_Id(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("배송지가 등록되지 않았습니다."));
    }


    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .addressId(address.getId())                          // Address 엔티티 PK
                .receiver(address.getUser().getUserName())    // Users 엔티티에서 이름
                .phone(address.getUser().getPhone())          // Users 엔티티에서 전화번호
                .mainAddress(address.getMainAddress())        // Address 테이블의 mainAddress
                .build();
    }

}

