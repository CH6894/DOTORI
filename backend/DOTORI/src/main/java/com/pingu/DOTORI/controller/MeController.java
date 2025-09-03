package com.pingu.DOTORI.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.security.JwtProvider;
import com.pingu.DOTORI.service.MyPageService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class MeController {

    private final MyPageService myPageService;
    private final JwtProvider jwtProvider;

    public MeController(MyPageService myPageService, JwtProvider jwtProvider) {
        this.myPageService = myPageService;
        this.jwtProvider = jwtProvider;
    }

    @GetMapping("/api/me")
    public ResponseEntity<?> me(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String email = jwtProvider.getSubject(token);

                Users user = myPageService.getProfileByEmail(email);
                if (user != null) {
                    return ResponseEntity.ok(user);
                }
            } catch (Exception e) {
                System.out.println("JWT 파싱 실패: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of("authenticated", false));
    }

    @PutMapping("/api/me")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request, HttpServletRequest req) {
        try {
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtProvider.getSubject(token);

                String nickName = request.get("nickName");
                if (nickName != null && !nickName.trim().isEmpty()) {
                    Users updatedUser = myPageService.updateProfileByEmail(email, nickName);
                    if (updatedUser != null) {
                        return ResponseEntity.ok(updatedUser);
                    }
                }
            }

            return ResponseEntity.badRequest().body(Map.of("error", "프로필 업데이트 실패"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "프로필 업데이트 중 오류가 발생했습니다"));
        }
    }
}