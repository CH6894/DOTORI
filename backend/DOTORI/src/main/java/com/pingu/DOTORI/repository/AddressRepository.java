package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.Address;
import com.pingu.DOTORI.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findFirstByUser(Users user);
}
