package com.pingu.DOTORI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository; // ✅ 추가
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.*;
import com.pingu.DOTORI.security.OAuth2SuccessHandler;
import com.pingu.DOTORI.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(OAuth2SuccessHandler oAuth2SuccessHandler, JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  AuthenticationEntryPoint restAuthEntryPoint() {
    return (request, response, authException) -> {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"UNAUTHORIZED\"}");
    };
  }

  @Bean(name = "mainSecurityFilterChain")
  SecurityFilterChain filterChain(HttpSecurity http,
      ClientRegistrationRepository repo) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .addFilterBefore(jwtAuthenticationFilter,
            org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/health", "/public/**").permitAll()
            .requestMatchers("/oauth2/**", "/login/**").permitAll()
            .requestMatchers("/static/**").permitAll()
            .requestMatchers("/assets/**").permitAll()
            .requestMatchers("/uploads/**").permitAll() // 업로드된 이미지 파일 접근 허용
            .requestMatchers("/open/**").permitAll()
            .requestMatchers("/api/inspections").permitAll() // 검수 신청은 인증 없이 허용
            .requestMatchers("/api/**").authenticated()
            .anyRequest().authenticated())
        .exceptionHandling(e -> e.authenticationEntryPoint(restAuthEntryPoint()))
        .oauth2Login(oauth -> oauth
            .authorizationEndpoint(auth -> auth.authorizationRequestResolver(new AlwaysReauthResolver(repo)))
            .successHandler(oAuth2SuccessHandler) // 로그인 성공 후 JWT 발급
        );

    return http.build();
  }

  @Bean(name = "apiCorsConfigurationSource")
  CorsConfigurationSource corsConfigurationSource() {
    var config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    source.registerCorsConfiguration("/oauth2/**", config);
    source.registerCorsConfiguration("/login/**", config);
    return source;
  }
}
