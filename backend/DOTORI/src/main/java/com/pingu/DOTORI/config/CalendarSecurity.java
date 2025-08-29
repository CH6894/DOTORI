package com.pingu.DOTORI.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class CalendarSecurity {

    // CalendarConfig에서 만든 CORS 빈을 명시적으로 사용
    private final CorsConfigurationSource corsSource;

    public CalendarSecurity(@Qualifier("calendarCorsSource") CorsConfigurationSource corsSource) {
        this.corsSource = corsSource;
    }

    // ✅ API 전용 보안 체인 (이름 분리 + 경로 한정)
    @Bean(name = "calendarSecurityFilterChain")
    @Order(200)
    public SecurityFilterChain calendarSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**") // 이 체인은 /api/** 만 적용
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsSource))
            .authorizeHttpRequests(reg -> reg
                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults()); // 데모용 Basic Auth

        return http.build();
    }

    // 이미 다른 UserDetailsService가 있으면 내 것은 만들지 않음(중복 방지)
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
            User.withUsername("admin").password("{noop}admin123").roles("ADMIN").build()
        );
    }
}
