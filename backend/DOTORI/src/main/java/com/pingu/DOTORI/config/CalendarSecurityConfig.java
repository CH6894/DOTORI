// src/main/java/com/pingu/DOTORI/config/SecurityConfig.java
package com.pingu.DOTORI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class CalendarSecurityConfig {

  // 1) /api/admin/** 전용 체인 (우선 적용)
  @Bean
  @Order(1)
  SecurityFilterChain admin(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/api/admin/**")
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll() // 개발 중 임시 오픈
        );
    return http.build();
  }

}