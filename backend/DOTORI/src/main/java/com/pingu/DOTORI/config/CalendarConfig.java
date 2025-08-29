
package com.pingu.DOTORI.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import java.util.List;

//com.pingu.DOTORI.config.CalendarConfig
@Configuration
@EnableMethodSecurity
public class CalendarConfig {

 // (기존 CORS Bean은 유지)
 @Bean(name = "calendarCorsConfigurationSource")
 @Order(199)
 CorsConfigurationSource corsConfigurationSource() {
     CorsConfiguration cfg = new CorsConfiguration();
     cfg.setAllowedOrigins(List.of("http://localhost:5173","http://127.0.0.1:5173","http://localhost:8080"));
     cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
     cfg.setAllowedHeaders(List.of("*"));
     cfg.setAllowCredentials(true);
     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
     source.registerCorsConfiguration("/**", cfg);
     return source;
 }

 // ✅ 이름 변경 + 경로 범위 분리 + (선택) 순서 지정
 @Bean(name = "apiSecurityFilterChain")
 @Order(200)
 SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
     http
         .securityMatcher("/api/**")              // 이 체인은 /api/** 에만 적용
         .csrf(csrf -> csrf.disable())
         .cors(Customizer.withDefaults())
         .authorizeHttpRequests(reg -> reg
             .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
             .requestMatchers("/api/admin/**").hasRole("ADMIN")
             .anyRequest().authenticated()
         )
         .httpBasic(Customizer.withDefaults());
     return http.build();
 }
}

