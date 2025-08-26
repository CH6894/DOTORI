package com.pingu.DOTORI.security;

import org.springframework.stereotype.Component;

@Component   // ✅ 스프링 빈으로 등록
public class JwtProvider {

    // JWT 발급 로직 (예시)
    public String createToken(String subject) {
        // TODO: JWT 생성 코드 (io.jsonwebtoken.Jwts 같은 라이브러리 사용)
        return "dummy-token-for-" + subject;
    }
}
