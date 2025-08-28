package com.pingu.DOTORI.service;

import org.springframework.stereotype.Service;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.repository.UsersRepository;
import jakarta.transaction.Transactional;

@Service
public class MyPageService {

    private final UsersRepository usersRepository;

    public MyPageService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public Users getProfileByEmail(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public Users updateProfileByEmail(String email, String nickName) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (nickName != null && !nickName.trim().isEmpty()) {
            user.setNickName(nickName);
        }

        return user;
    }
}