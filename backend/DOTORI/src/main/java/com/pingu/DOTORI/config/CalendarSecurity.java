package com.pingu.DOTORI.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;


@Configuration
@EnableMethodSecurity
public class CalendarSecurity {

    // 이름은 그대로 filterChain이어도, 위 체인과 경로가 달라 공존 가능
    @Bean
    @Order(100) // (선택) 다른 체인보다 먼저 적용하고 싶을 때
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**") // 전체 또는 /api/** 제외한 다른 경로
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // 예: 웹은 모두 허용. 필요에 맞게 작성
            );
        return http.build();
    }
}
