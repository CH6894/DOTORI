package com.pingu.DOTORI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository; // ✅ 추가
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import com.pingu.DOTORI.security.OAuth2SuccessHandler;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final OAuth2SuccessHandler oAuth2SuccessHandler;

  public SecurityConfig(OAuth2SuccessHandler oAuth2SuccessHandler) {
    this.oAuth2SuccessHandler = oAuth2SuccessHandler;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http,
                                  ClientRegistrationRepository repo) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> {})
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/health", "/public/**").permitAll()
            .requestMatchers("/oauth2/**", "/login/**").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth -> oauth
            .authorizationEndpoint(auth -> 
                auth.authorizationRequestResolver(new AlwaysReauthResolver(repo)) // ✅ 자동 로그인 방지
            )
            .successHandler(oAuth2SuccessHandler) // 로그인 성공 후 JWT 발급
        );

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    var config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173","http://localhost:8081"));
    config.setAllowedMethods(List.of("GET","POST","DELETE","OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
