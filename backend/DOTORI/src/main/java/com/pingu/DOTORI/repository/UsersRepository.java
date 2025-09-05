package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    Optional<Users> findByNickName(String nickName);

    Optional<Users> findByPhone(String phone);
}