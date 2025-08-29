package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.Address;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;



public interface AddressRepository extends JpaRepository<Address, Long> {
    // 해당 유저의 기본 배송지 1개만 가져오기
	 Optional<Address> findFirstByUser_Id(Long userId);
	 
	// 필요하다면 여러 개 가져오기
	List<Address> findByUser_Id(Long userId);
}
