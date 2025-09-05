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
        .anyRequest().permitAll()   // 개발 중 임시 오픈
      );
    return http.build();
  }
	//2) 나머지 전체 (**반드시 마지막**, any request)
	@Bean
	@Order(2)
	SecurityFilterChain main(HttpSecurity http) throws Exception {
	 http
	   .securityMatcher("/api/**")
	   .cors(Customizer.withDefaults())
	   .csrf(csrf -> csrf.disable())
	   .authorizeHttpRequests(auth -> auth
	     .requestMatchers("/api/public/**", "/actuator/**").permitAll()
	     .anyRequest().authenticated()   // <-- 여기만 바뀜: 인증된 사용자만 접근
	   )
	   .oauth2Login(Customizer.withDefaults()); // <-- 여러 아이디(OAuth2 로그인) 허용
	 return http.build();
}

  // // 2) 나머지 전체 (**반드시 마지막**, any request)
  // @Bean
  // @Order(2)
  // SecurityFilterChain main(HttpSecurity http) throws Exception {
  //   http
  //     .securityMatcher("/**")
  //     .cors(Customizer.withDefaults())
  //     .csrf(csrf -> csrf.disable())
  //     .authorizeHttpRequests(auth -> auth
  //       .requestMatchers("/api/public/**", "/actuator/**").permitAll()
  //       .anyRequest().permitAll()  // 필요 시 인증으로 변경
  //     );
  //   return http.build();
  // }
}
