package com.pingu.DOTORI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository; // ✅ 추가
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import com.pingu.DOTORI.security.OAuth2SuccessHandler;
import com.pingu.DOTORI.security.JwtAuthenticationFilter;

import java.util.List;

import com.pingu.DOTORI.config.AlwaysReauthResolver;

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
  SecurityFilterChain filterChain(HttpSecurity http,
      ClientRegistrationRepository repo) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> {
        })
        .addFilterBefore(jwtAuthenticationFilter,
            org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
        	    .requestMatchers("/", "/health", "/public/**").permitAll()
        	    .requestMatchers("/oauth2/**", "/login/**").permitAll()
        	    .requestMatchers("/static/**").permitAll()
        	    .requestMatchers("/api/inspections/**").authenticated() // ✅ 로그인 필요
        	    .requestMatchers("/api/**").permitAll()
        	    .anyRequest().authenticated()
        );

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    var config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8081"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setExposedHeaders(List.of("Authorization"));

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }
}
